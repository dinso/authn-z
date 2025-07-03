package org.example.multi_tenant_app.web.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class RolePermissionAssignmentDTO {

    private UUID id; // ID of the assignment record itself

    @NotNull(message = "Tenant ID cannot be null")
    private UUID tenantId; // Corresponds to the tenant of the Role

    @NotNull(message = "Role ID cannot be null")
    private UUID roleId;

    @NotNull(message = "Permission ID cannot be null")
    private UUID permissionId;

    private LocalDateTime assignedAt;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public UUID getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(UUID permissionId) {
        this.permissionId = permissionId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
