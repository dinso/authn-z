package org.example.multi_tenant_app.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.IOException;
import java.util.UUID;

@Provider
@Priority(100) // Ensure it runs relatively early, but after authentication potentially
public class TenantIdFilter implements ContainerRequestFilter {

    @Inject
    TenantContext tenantContext;

    @Inject
    SecurityIdentity securityIdentity; // Injected to access authenticated user details

    // JsonWebToken jwtPrincipal might not be correctly initialized or might be a
    // simple principal when OIDC is disabled and @TestSecurity is used.
    // It's safer to get it from SecurityIdentity and check its type.
    // @Inject
    // JsonWebToken jwtPrincipal;

    private static final String TENANT_ID_HEADER = "X-Tenant-ID";
    private static final String TENANT_ID_JWT_CLAIM = "tenant_id"; // Common claim name, adjust if different

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UUID tenantId = null;

        // 1. Try to extract from JWT if user is authenticated and the principal is a JsonWebToken
        if (securityIdentity != null && securityIdentity.getPrincipal() instanceof JsonWebToken) {
            JsonWebToken actualJwt = (JsonWebToken) securityIdentity.getPrincipal();
            if (actualJwt.containsClaim(TENANT_ID_JWT_CLAIM)) {
                try {
                    String tenantIdStr = actualJwt.getClaim(TENANT_ID_JWT_CLAIM);
                    if (tenantIdStr != null && !tenantIdStr.isBlank()) {
                        tenantId = UUID.fromString(tenantIdStr);
                    }
                } catch (Exception e) {
                    // Log error: failed to parse tenant_id claim from JWT
                    System.err.println("Error parsing tenant_id claim from JWT: " + e.getMessage());
                }
            }
        }

        // 2. If not found in JWT (or if principal wasn't a JWT), try X-Tenant-ID header
        // This could be conditional based on path, e.g. if requestContext.getUriInfo().getPath().startsWith("/mcp")
        if (tenantId == null) {
            String tenantIdHeaderValue = requestContext.getHeaderString(TENANT_ID_HEADER);
            if (tenantIdHeaderValue != null && !tenantIdHeaderValue.isBlank()) {
                try {
                    tenantId = UUID.fromString(tenantIdHeaderValue);
                } catch (IllegalArgumentException e) {
                    // Log error: Invalid X-Tenant-ID header format
                    System.err.println("Invalid X-Tenant-ID header format: " + tenantIdHeaderValue);
                    // Optionally, could abort request with 400 Bad Request here if header is present but invalid
                    // requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity("Invalid X-Tenant-ID header format.").build());
                    // return;
                }
            }
        }

        if (tenantId != null) {
            tenantContext.setCurrentTenantId(tenantId);
            System.out.println("TenantContext populated with Tenant ID: " + tenantId + " for path: " + requestContext.getUriInfo().getPath());
        } else {
            // No tenant ID found in JWT or header.
            // Behavior here depends on requirements:
            // - Allow request to proceed (tenant-agnostic endpoints, or public parts of tenant endpoints)
            // - Abort with 400/401/403 if tenant ID is strictly required for most/all paths
            // For now, let it proceed. Services that require a tenantId will fail if TenantContext.getRequiredTenantId() is called.
            System.out.println("No Tenant ID found in JWT or header for path: " + requestContext.getUriInfo().getPath());
        }
    }
}
