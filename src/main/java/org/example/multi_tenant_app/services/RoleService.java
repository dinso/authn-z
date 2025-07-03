package org.example.multi_tenant_app.services;

import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.multi_tenant_app.data.entities.Role;
import org.example.multi_tenant_app.security.TenantContext;
import org.example.multi_tenant_app.web.dtos.RoleDTO;
import org.hibernate.Filter;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleService {

    @Inject
    TenantContext tenantContext;

    // --- Utility method to enable tenant filter using TenantContext ---
    private void enableTenantFilter() {
        UUID currentTenantId = tenantContext.getRequiredTenantId(); // Throws if not set
        Session session = Panache.getEntityManager().unwrap(Session.class);
        // Check if filter is already enabled with the same parameter to avoid re-setting if not necessary
        Filter enabledFilter = session.getEnabledFilter("tenantFilter");
        if (enabledFilter == null || !enabledFilter.getParameterValue("tenantId").equals(currentTenantId)) {
            session.enableFilter("tenantFilter").setParameter("tenantId", currentTenantId);
        }
    }

    private RoleDTO convertToDTO(Role role) {
        if (role == null) return null;
        RoleDTO dto = new RoleDTO();
        dto.setId(role.id);
        dto.setTenantId(role.tenantId);
        dto.setName(role.name);
        dto.setDescription(role.description);
        dto.setSystemRole(role.isSystemRole);
        dto.setCreatedAt(role.createdAt);
        dto.setUpdatedAt(role.updatedAt);
        return dto;
    }

    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) { // tenantId parameter removed, will get from context
        UUID currentTenantId = tenantContext.getRequiredTenantId();
        // enableTenantFilter(); // Not strictly needed before persist if tenantId is set directly on entity from context

        // Validate or set tenantId from context if DTO allows it, or if it's part of validation logic
        if (roleDTO.getTenantId() != null && !roleDTO.getTenantId().equals(currentTenantId)) {
            throw new SecurityException("Tenant ID in DTO does not match current tenant context.");
        }

        Role role = new Role();
        role.tenantId = currentTenantId; // Set tenantId from context
        role.name = roleDTO.getName();
        role.description = roleDTO.getDescription();
        role.isSystemRole = roleDTO.isSystemRole();
        role.createdAt = LocalDateTime.now();
        role.updatedAt = LocalDateTime.now();

        role.persist();
        return convertToDTO(role);
    }

    public Optional<RoleDTO> getRoleById(UUID roleId) { // tenantId parameter removed
        enableTenantFilter(); // Uses tenantId from context
        // findByIdOptional will be affected by the enabled tenant filter
        return Role.<Role>findByIdOptional(roleId).map(this::convertToDTO);
    }

    public List<RoleDTO> getRolesByTenant() { // tenantId parameter removed
        enableTenantFilter(); // Uses tenantId from context
        // listAll will be affected by the enabled tenant filter
        return Role.<Role>listAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<RoleDTO> updateRole(UUID roleId, RoleDTO roleDTO) { // tenantId parameter removed
        UUID currentTenantId = tenantContext.getRequiredTenantId();
        enableTenantFilter(); // Uses tenantId from context

        // Validate tenantId in DTO if present
        if (roleDTO.getTenantId() != null && !roleDTO.getTenantId().equals(currentTenantId)) {
            throw new SecurityException("Tenant ID in DTO does not match current tenant context for update.");
        }

        Optional<Role> existingRoleOpt = Role.findByIdOptional(roleId); // Filter ensures it's from the current tenant
        if (existingRoleOpt.isEmpty()) {
            return Optional.empty();
        }

        Role roleToUpdate = existingRoleOpt.get();
        if (roleToUpdate.isSystemRole) {
            // Potentially throw a ForbiddenException or similar if system roles cannot be updated by tenants
            // For now, let's assume some fields of system roles might be updatable, or this check is elsewhere.
        }

        roleToUpdate.name = roleDTO.getName();
        roleToUpdate.description = roleDTO.getDescription();
        // tenantId and isSystemRole are generally not updatable by typical users.
        roleToUpdate.updatedAt = LocalDateTime.now();

        roleToUpdate.persist(); // Panache handles update
        return Optional.of(convertToDTO(roleToUpdate));
    }

    @Transactional
    public boolean deleteRole(UUID roleId) { // tenantId parameter removed
        enableTenantFilter(); // Uses tenantId from context
        Optional<Role> roleOpt = Role.findByIdOptional(roleId); // Filter ensures it's from the current tenant
        if (roleOpt.isPresent()) {
            if (roleOpt.get().isSystemRole) {
                // Prevent deletion of system roles or throw specific exception
                return false; // Or throw new ForbiddenException("System roles cannot be deleted.");
            }
            // TODO: Add logic to check if role is assigned to any users before deletion
            // For now, direct delete:
            return Role.deleteById(roleId);
        }
        return false;
    }
}