package org.example.multi_tenant_app.services;

import org.example.multi_tenant_app.data.entities.UserAccount;
import org.example.multi_tenant_app.data.entities.Role;
import org.example.multi_tenant_app.data.entities.UserRoleAssignment;
import org.example.multi_tenant_app.web.dtos.UserRoleAssignmentDTO; // To be created

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
public class UserRoleAssignmentService {

    // For validating existence of user and role, and ensuring they belong to the same tenant.
    // These would typically be full services. For now, assuming direct Panache entity access for brevity.
    // @Inject
    // UserService userService;
    // @Inject
    // RoleService roleService;

    private void enableTenantFilter(UUID tenantId) {
        Session session = Panache.getEntityManager().unwrap(Session.class);
        // Only enable if not already enabled or if tenantId changed
        if (session.getEnabledFilter("tenantFilter") == null ||
            !session.getEnabledFilter("tenantFilter").getParameter("tenantId").equals(tenantId)) {
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        }
    }

    private UserRoleAssignmentDTO convertToDTO(UserRoleAssignment assignment) {
        if (assignment == null) return null;
        UserRoleAssignmentDTO dto = new UserRoleAssignmentDTO();
        dto.setId(assignment.id);
        dto.setTenantId(assignment.tenantId);
        dto.setUserAccountId(assignment.userAccountId);
        dto.setRoleId(assignment.roleId);
        dto.setAssignedAt(assignment.assignedAt);
        return dto;
    }

    @Transactional
    public UserRoleAssignmentDTO assignRoleToUser(UUID tenantId, UUID userId, UUID roleId) {
        enableTenantFilter(tenantId); // Enable filter for subsequent checks if any

        // 1. Validate tenantId consistency (path vs. potential DTO, though not used directly here)
        // 2. Verify UserAccount exists and belongs to the tenant
        UserAccount user = UserAccount.<UserAccount>find("id = ?1 and tenantId = ?2", userId, tenantId).firstResult();
        if (user == null) {
            throw new NotFoundException("UserAccount not found in this tenant.");
        }

        // 3. Verify Role exists and belongs to the tenant
        Role role = Role.<Role>find("id = ?1 and tenantId = ?2", roleId, tenantId).firstResult();
        if (role == null) {
            throw new NotFoundException("Role not found in this tenant.");
        }

        // 4. Check if assignment already exists
        long existingAssignments = UserRoleAssignment.count("tenantId = ?1 and userAccountId = ?2 and roleId = ?3", tenantId, userId, roleId);
        if (existingAssignments > 0) {
            // Or return existing, or throw a conflict exception
            // For now, let's assume we just don't create a duplicate
             UserRoleAssignment existing = UserRoleAssignment.<UserRoleAssignment>find("tenantId = ?1 and userAccountId = ?2 and roleId = ?3", tenantId, userId, roleId).firstResult();
            return convertToDTO(existing);
        }

        UserRoleAssignment newAssignment = new UserRoleAssignment(tenantId, userId, roleId);
        newAssignment.persist();
        return convertToDTO(newAssignment);
    }

    @Transactional
    public boolean removeRoleFromUser(UUID tenantId, UUID userId, UUID roleId) {
        enableTenantFilter(tenantId);

        // Optional: Validate user and role existence first, similar to assignRoleToUser
        // UserAccount user = UserAccount.<UserAccount>find("id = ?1 and tenantId = ?2", userId, tenantId).firstResult();
        // if (user == null) throw new NotFoundException("User not found");
        // Role role = Role.<Role>find("id = ?1 and tenantId = ?2", roleId, tenantId).firstResult();
        // if (role == null) throw new NotFoundException("Role not found");

        long deletedCount = UserRoleAssignment.delete("tenantId = ?1 and userAccountId = ?2 and roleId = ?3", tenantId, userId, roleId);
        return deletedCount > 0;
    }

    public List<RoleDTO> getRolesForUser(UUID tenantId, UUID userId) {
        enableTenantFilter(tenantId);

        // Verify UserAccount exists and belongs to the tenant
        UserAccount user = UserAccount.<UserAccount>find("id = ?1 and tenantId = ?2", userId, tenantId).firstResult();
        if (user == null) {
            throw new NotFoundException("UserAccount not found in this tenant.");
        }

        List<UserRoleAssignment> assignments = UserRoleAssignment.list("tenantId = ?1 and userAccountId = ?2", tenantId, userId);

        List<UUID> roleIds = assignments.stream().map(ura -> ura.roleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return List.of();
        }

        // Note: The tenant filter is already enabled. If RoleService also enables it, it's fine.
        // This assumes RoleService's listByIds or similar method respects the filter or internally filters by tenant.
        // For simplicity, directly querying Role here.
        return Role.<Role>list("id in ?1 and tenantId = ?2", roleIds, tenantId).stream()
            .map(role -> {
                RoleDTO dto = new RoleDTO();
                dto.setId(role.id);
                dto.setTenantId(role.tenantId);
                dto.setName(role.name);
                dto.setDescription(role.description);
                dto.setSystemRole(role.isSystemRole);
                dto.setCreatedAt(role.createdAt);
                dto.setUpdatedAt(role.updatedAt);
                return dto;
            })
            .collect(Collectors.toList());
    }
}
