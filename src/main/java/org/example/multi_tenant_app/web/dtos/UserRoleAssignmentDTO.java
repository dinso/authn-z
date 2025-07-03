package org.example.multi_tenant_app.web.dtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserRoleAssignmentDTO {

    private UUID id; // ID of the assignment record itself

    @NotNull(message = "Tenant ID cannot be null")
    private UUID tenantId;

    @NotNull(message = "User Account ID cannot be null")
    private UUID userAccountId;

    @NotNull(message = "Role ID cannot be null")
    private UUID roleId;

    private LocalDateTime assignedAt;

    // Optional: Include representations of the user or role if needed in responses
    // private UserAccountDTO userAccount;
    // private RoleDTO role;

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

    public UUID getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(UUID userAccountId) {
        this.userAccountId = userAccountId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
