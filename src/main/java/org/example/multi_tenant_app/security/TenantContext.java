package org.example.multi_tenant_app.security;

import jakarta.enterprise.context.RequestScoped;

import java.util.Optional;
import java.util.UUID;

/**
 * A request-scoped bean to hold the current tenant's ID.
 * This context is intended to be populated by a filter/interceptor
 * that extracts tenant information from the incoming request (e.g., JWT, header).
 */
@RequestScoped
public class TenantContext {

    private UUID currentTenantId;

    public Optional<UUID> getCurrentTenantId() {
        return Optional.ofNullable(currentTenantId);
    }

    public void setCurrentTenantId(UUID currentTenantId) {
        if (this.currentTenantId != null && !this.currentTenantId.equals(currentTenantId)) {
            // This case should ideally not happen within a single request if set correctly once.
            // Log a warning or throw an exception if tenant ID is being changed mid-request.
            // For now, allow overwrite, but this could be a point of caution.
            System.err.println("Warning: TenantContext currentTenantId is being overwritten. Old: " + this.currentTenantId + ", New: " + currentTenantId);
        }
        this.currentTenantId = currentTenantId;
    }

    public void clear() {
        this.currentTenantId = null;
    }

    public boolean isTenantIdAvailable() {
        return this.currentTenantId != null;
    }

    /**
     * Gets the current tenant ID. Throws an IllegalStateException if no tenant ID is set.
     * Use this when a tenant ID is mandatory for the operation.
     * @return The current tenant ID.
     * @throws IllegalStateException if the tenant ID is not available in the context.
     */
    public UUID getRequiredTenantId() {
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant ID is not available in the current context.");
        }
        return currentTenantId;
    }
}
