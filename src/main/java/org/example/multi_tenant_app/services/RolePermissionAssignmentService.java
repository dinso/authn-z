package org.example.multi_tenant_app.services;

import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.example.multi_tenant_app.data.entities.Permission;
import org.example.multi_tenant_app.data.entities.Role;
import org.example.multi_tenant_app.data.entities.RolePermissionAssignment;
import org.example.multi_tenant_app.security.TenantContext;
import org.example.multi_tenant_app.web.dtos.PermissionDTO;
import org.example.multi_tenant_app.web.dtos.RolePermissionAssignmentDTO;
import org.hibernate.Filter;
import org.hibernate.Session;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RolePermissionAssignmentService {

    // To validate role and permission existence.
    // @Inject RoleService roleService; // Assuming RoleService can fetch a role by ID and tenant ID
    // For Permission, it's global, so direct access or a simple PermissionService could be used.

    @Inject
    TenantContext tenantContext;

    private void enableTenantFilterForRole() { // tenantId parameter removed
        UUID currentTenantId = tenantContext.getRequiredTenantId();
        Session session = Panache.getEntityManager().unwrap(Session.class);
        Filter enabledFilter = session.getEnabledFilter("tenantFilter");
        if (enabledFilter == null || !enabledFilter.getParameterValue("tenantId").equals(currentTenantId)) {
            session.enableFilter("tenantFilter").setParameter("tenantId", currentTenantId);
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
    public RolePermissionAssignmentDTO assignPermissionToRole(UUID roleId, UUID permissionId) { // tenantId removed
        UUID currentTenantId = tenantContext.getRequiredTenantId();
        enableTenantFilterForRole(); // Filter applies to Role entity checks using tenantId from context

        // 1. Verify Role exists and belongs to the current tenant (filter will apply)
        Role role = Role.findById(roleId);
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

        // 3. Check if assignment already exists (Filter will apply to RolePermissionAssignment as well)
        RolePermissionAssignment existingAssignment = RolePermissionAssignment
                .<RolePermissionAssignment>find("roleId = ?1 and permissionId = ?2", roleId, permissionId)
                .firstResult(); // Filter ensures this is for the current tenant via the role's tenantId linkage in the assignment

        if (existingAssignment != null) {
            return convertToDTO(existingAssignment);
        }

        RolePermissionAssignment newAssignment = new RolePermissionAssignment(currentTenantId, roleId, permissionId);
        newAssignment.persist();
        return convertToDTO(newAssignment);
    }

    @Transactional
    public boolean removePermissionFromRole(UUID roleId, UUID permissionId) { // tenantId removed
        UUID currentTenantId = tenantContext.getRequiredTenantId();
        enableTenantFilterForRole(); // Ensure role check is tenanted (and subsequent RPA query if it relies on filtered Role)

        Role role = Role.findById(roleId); // Filter ensures it's the correct tenant's role
        if (role == null) {
            throw new NotFoundException("Role not found in this tenant.");
        }
        if (role.isSystemRole) {
            // Potentially throw an error
        }

        // Delete query needs to be explicitly scoped by tenant_id for safety,
        // as the filter on RolePermissionAssignment might not be active for delete by query.
        long deletedCount = RolePermissionAssignment.delete("tenantId = ?1 and roleId = ?2 and permissionId = ?3", currentTenantId, roleId, permissionId);
        return deletedCount > 0;
    }

    public List<PermissionDTO> getPermissionsForRole(UUID roleId) { // tenantId removed
        enableTenantFilterForRole(); // Ensure role check is tenanted

        Role role = Role.findById(roleId); // Filter ensures it's the correct tenant's role
        if (role == null) {
            throw new NotFoundException("Role not found in this tenant.");
        }

        // Filter will apply to this list operation on RolePermissionAssignment
        List<RolePermissionAssignment> assignments = RolePermissionAssignment.list("roleId = ?1", roleId);

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