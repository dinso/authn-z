package org.example.multi_tenant_app.services;

import org.example.multi_tenant_app.data.entities.Permission;
import org.example.multi_tenant_app.data.entities.Role;
import org.example.multi_tenant_app.data.entities.RolePermissionAssignment;
import org.example.multi_tenant_app.web.dtos.PermissionDTO; // To be created
import org.example.multi_tenant_app.web.dtos.RolePermissionAssignmentDTO; // To be created


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.inject.Inject;

import org.hibernate.Session;
import io.quarkus.hibernate.orm.panache.Panache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RolePermissionAssignmentService {

    // To validate role and permission existence.
    // @Inject RoleService roleService; // Assuming RoleService can fetch a role by ID and tenant ID
    // For Permission, it's global, so direct access or a simple PermissionService could be used.

    private void enableTenantFilterForRole(UUID tenantId) {
        Session session = Panache.getEntityManager().unwrap(Session.class);
        if (session.getEnabledFilter("tenantFilter") == null ||
            !session.getEnabledFilter("tenantFilter").getParameter("tenantId").equals(tenantId)) {
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        }
    }

    private RolePermissionAssignmentDTO convertToDTO(RolePermissionAssignment assignment) {
        if (assignment == null) return null;
        RolePermissionAssignmentDTO dto = new RolePermissionAssignmentDTO();
        dto.setId(assignment.id);
        dto.setTenantId(assignment.tenantId);
        dto.setRoleId(assignment.roleId);
        dto.setPermissionId(assignment.permissionId);
        dto.setAssignedAt(assignment.assignedAt);
        return dto;
    }

    private PermissionDTO convertPermissionToDTO(Permission permission) {
        if (permission == null) return null;
        PermissionDTO dto = new PermissionDTO();
        dto.setId(permission.id);
        dto.setName(permission.name);
        dto.setDescription(permission.description);
        dto.setCreatedAt(permission.createdAt);
        dto.setUpdatedAt(permission.updatedAt);
        return dto;
    }


    @Transactional
    public RolePermissionAssignmentDTO assignPermissionToRole(UUID tenantId, UUID roleId, UUID permissionId) {
        enableTenantFilterForRole(tenantId); // Filter applies to Role entity checks

        // 1. Verify Role exists and belongs to the tenant
        Role role = Role.<Role>find("id = ?1 and tenantId = ?2", roleId, tenantId).firstResult();
        if (role == null) {
            throw new NotFoundException("Role not found in this tenant.");
        }
        if (role.isSystemRole) {
            // Potentially throw an error if system roles' permissions cannot be modified
            // Or this logic is handled at a higher level (e.g., specific admin APIs)
        }

        // 2. Verify Permission exists (Permissions are global)
        Permission permission = Permission.findById(permissionId);
        if (permission == null) {
            throw new NotFoundException("Permission not found.");
        }

        // 3. Check if assignment already exists
        long existingAssignments = RolePermissionAssignment.count("tenantId = ?1 and roleId = ?2 and permissionId = ?3", tenantId, roleId, permissionId);
        if (existingAssignments > 0) {
            RolePermissionAssignment existing = RolePermissionAssignment.<RolePermissionAssignment>find("tenantId = ?1 and roleId = ?2 and permissionId = ?3", tenantId, roleId, permissionId).firstResult();
            return convertToDTO(existing);
        }

        RolePermissionAssignment newAssignment = new RolePermissionAssignment(tenantId, roleId, permissionId);
        newAssignment.persist();
        return convertToDTO(newAssignment);
    }

    @Transactional
    public boolean removePermissionFromRole(UUID tenantId, UUID roleId, UUID permissionId) {
        enableTenantFilterForRole(tenantId); // Ensure role check is tenanted

        Role role = Role.<Role>find("id = ?1 and tenantId = ?2", roleId, tenantId).firstResult();
        if (role == null) {
            throw new NotFoundException("Role not found in this tenant.");
        }
        if (role.isSystemRole) {
            // Potentially throw an error
        }
        // Permission existence check could be added here too if desired, but FK constraint handles it.

        long deletedCount = RolePermissionAssignment.delete("tenantId = ?1 and roleId = ?2 and permissionId = ?3", tenantId, roleId, permissionId);
        return deletedCount > 0;
    }

    public List<PermissionDTO> getPermissionsForRole(UUID tenantId, UUID roleId) {
        enableTenantFilterForRole(tenantId); // Ensure role check is tenanted

        Role role = Role.<Role>find("id = ?1 and tenantId = ?2", roleId, tenantId).firstResult();
        if (role == null) {
            throw new NotFoundException("Role not found in this tenant.");
        }

        List<RolePermissionAssignment> assignments = RolePermissionAssignment.list("tenantId = ?1 and roleId = ?2", tenantId, roleId);

        List<UUID> permissionIds = assignments.stream().map(rpa -> rpa.permissionId).collect(Collectors.toList());

        if (permissionIds.isEmpty()) {
            return List.of();
        }

        // Permissions are global, so no tenant filter needed for Permission.list(...)
        return Permission.<Permission>list("id in ?1", permissionIds).stream()
            .map(this::convertPermissionToDTO)
            .collect(Collectors.toList());
    }

    // Method to list all available global permissions (useful for UIs)
    public List<PermissionDTO> getAllGlobalPermissions() {
        return Permission.<Permission>listAll().stream()
            .map(this::convertPermissionToDTO)
            .collect(Collectors.toList());
    }
}
