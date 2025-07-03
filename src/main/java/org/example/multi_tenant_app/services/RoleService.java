package org.example.multi_tenant_app.services;

import org.example.multi_tenant_app.data.entities.Role;
import org.example.multi_tenant_app.web.dtos.RoleDTO; // Will create this DTO later

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import org.hibernate.Session;
import io.quarkus.hibernate.orm.panache.Panache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleService {

    // --- Utility method to enable tenant filter ---
    // This should ideally be handled more globally, e.g., via a CDI interceptor or a base repository/service class.
    // For now, it's explicitly called in each relevant public method.
    private void enableTenantFilter(UUID tenantId) {
        Session session = Panache.getEntityManager().unwrap(Session.class);
        if (session.getEnabledFilter("tenantFilter") == null ||
            !session.getEnabledFilter("tenantFilter").getParameter("tenantId").equals(tenantId)) {
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
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
    public RoleDTO createRole(UUID tenantId, RoleDTO roleDTO) {
        enableTenantFilter(tenantId); // Ensure operations are within tenant context for safety, though creation is direct.

        if (!tenantId.equals(roleDTO.getTenantId())) {
            // Or throw an exception if tenantId in DTO is not allowed or conflicts path {tenantId}
            // For creation, we typically trust the path {tenantId} and set it.
            roleDTO.setTenantId(tenantId);
        }

        Role role = new Role();
        role.tenantId = tenantId;
        role.name = roleDTO.getName();
        role.description = roleDTO.getDescription();
        role.isSystemRole = roleDTO.isSystemRole(); // Typically false for tenant-created roles
        role.createdAt = LocalDateTime.now();
        role.updatedAt = LocalDateTime.now();

        role.persist();
        return convertToDTO(role);
    }

    public Optional<RoleDTO> getRoleById(UUID tenantId, UUID roleId) {
        enableTenantFilter(tenantId);
        return Role.<Role>findByIdOptional(roleId).map(this::convertToDTO);
    }

    public List<RoleDTO> getRolesByTenant(UUID tenantId) {
        enableTenantFilter(tenantId);
        // The filter should ensure only roles for the given tenantId are returned.
        // If not using filter for list(), then: List<Role> roles = Role.list("tenantId", tenantId);
        return Role.<Role>listAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<RoleDTO> updateRole(UUID tenantId, UUID roleId, RoleDTO roleDTO) {
        enableTenantFilter(tenantId);
        Optional<Role> existingRoleOpt = Role.findByIdOptional(roleId);
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
    public boolean deleteRole(UUID tenantId, UUID roleId) {
        enableTenantFilter(tenantId);
        Optional<Role> roleOpt = Role.findByIdOptional(roleId);
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
