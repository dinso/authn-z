package org.example.multi_tenant_app.services;

import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.example.multi_tenant_app.data.entities.Role;
import org.example.multi_tenant_app.data.entities.UserAccount;
import org.example.multi_tenant_app.data.entities.UserRoleAssignment;
import org.example.multi_tenant_app.security.TenantContext;
import org.example.multi_tenant_app.web.dtos.RoleDTO;
import org.example.multi_tenant_app.web.dtos.UserRoleAssignmentDTO;
import org.hibernate.Filter;
import org.hibernate.Session;

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

    @Inject
    TenantContext tenantContext;

    private void enableTenantFilter() {
        UUID currentTenantId = tenantContext.getRequiredTenantId();
        Session session = Panache.getEntityManager().unwrap(Session.class);
        // Only enable if not already enabled or if tenantId changed
        Filter enabledFilter = session.getEnabledFilter("tenantFilter");
        if (enabledFilter == null || !enabledFilter.getParameterValue("tenantId").equals(currentTenantId)) {
            session.enableFilter("tenantFilter").setParameter("tenantId", currentTenantId);
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
    public UserRoleAssignmentDTO assignRoleToUser(UUID userId, UUID roleId) { // tenantId parameter removed
        UUID currentTenantId = tenantContext.getRequiredTenantId();
        enableTenantFilter(); // Enable filter for subsequent checks

        // 1. Verify UserAccount exists and belongs to the current tenant
        // The enabled filter will apply to these Panache operations.
        UserAccount user = UserAccount.findById(userId);
        if (user == null) { // Filter would make it null if not in current tenant
            throw new NotFoundException("UserAccount not found in this tenant.");
        }

        // 2. Verify Role exists and belongs to the current tenant
        Role role = Role.findById(roleId);
        if (role == null) { // Filter would make it null if not in current tenant
            throw new NotFoundException("Role not found in this tenant.");
        }

        // 3. Check if assignment already exists (Filter applies to UserRoleAssignment as well)
        UserRoleAssignment existingAssignment = UserRoleAssignment
                .<UserRoleAssignment>find("userAccountId = ?1 and roleId = ?2", userId, roleId)
                .firstResult(); // Filter ensures it's for the current tenant

        if (existingAssignment != null) {
            return convertToDTO(existingAssignment);
        }

        UserRoleAssignment newAssignment = new UserRoleAssignment(currentTenantId, userId, roleId);
        newAssignment.persist();
        return convertToDTO(newAssignment);
    }

    @Transactional
    public boolean removeRoleFromUser(UUID userId, UUID roleId) { // tenantId parameter removed
        UUID currentTenantId = tenantContext.getRequiredTenantId();
        enableTenantFilter();

        // The delete operation will also be subject to the tenant filter if it's on UserRoleAssignment
        // and if the query implicitly involves tenantId or if Panache applies it broadly.
        // Explicitly adding tenantId to delete query for safety.
        long deletedCount = UserRoleAssignment.delete("tenantId = ?1 and userAccountId = ?2 and roleId = ?3", currentTenantId, userId, roleId);
        return deletedCount > 0;
    }

    public List<RoleDTO> getRolesForUser(UUID userId) { // tenantId parameter removed
        UUID currentTenantId = tenantContext.getRequiredTenantId();
        enableTenantFilter();

        // Verify UserAccount exists (filter will ensure it's for the current tenant)
        UserAccount user = UserAccount.findById(userId);
        if (user == null) {
            throw new NotFoundException("UserAccount not found in this tenant.");
        }

        // Filter will apply to this list operation on UserRoleAssignment
        List<UserRoleAssignment> assignments = UserRoleAssignment.list("userAccountId = ?1", userId);

        List<UUID> roleIds = assignments.stream().map(ura -> ura.roleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return List.of();
        }

        // Filter is already enabled and will apply to this Role query
        return Role.<Role>list("id in ?1", roleIds).stream()
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