package org.example.multi_tenant_app.web.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

// Using Jakarta Bean Validation API for input validation (optional, but good practice)
// Add quarkus-hibernate-validator if not already present for this to work
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class RoleDTO {

    private UUID id;

    @NotNull(message = "Tenant ID cannot be null for a role")
    private UUID tenantId;

    @NotBlank(message = "Role name cannot be blank")
    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    private String name;

    @Size(max = 255, message = "Role description cannot exceed 255 characters")
    private String description;

    private boolean isSystemRole; // Typically not set by user input for new roles

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSystemRole() {
        return isSystemRole;
    }

    public void setSystemRole(boolean systemRole) {
        isSystemRole = systemRole;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // toString, equals, hashCode if needed
}
