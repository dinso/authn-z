package org.example.multi_tenant_app.web.controllers;

import org.example.multi_tenant_app.services.RoleService;
import org.example.multi_tenant_app.web.dtos.RoleDTO;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.annotation.security.RolesAllowed; // Added for security

import java.util.List;
import java.util.UUID;

@Path("/api/v1/tenants/{tenantId}/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleResource {

    @Inject
    RoleService roleService;

    @POST
    @RolesAllowed({"tenant-admin", "system-admin"}) // Only tenant or system admins can create roles
    public Response createRole(@PathParam("tenantId") UUID tenantId, @Valid RoleDTO roleDTO) {
        // TenantId from path is used by TenantIdFilter to set context.
        // Service method no longer takes tenantId directly.
        // Optional: Validate roleDTO.getTenantId() against context if it's set in DTO.
        if (roleDTO.getTenantId() != null && !roleDTO.getTenantId().equals(tenantId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Tenant ID in DTO must match tenant ID in path or be null.\"}")
                    .build();
        }
        roleDTO.setTenantId(tenantId); // Ensure DTO has tenantId consistent with context for service layer if needed

        RoleDTO createdRole = roleService.createRole(roleDTO);
        return Response.created(
                UriBuilder.fromResource(RoleResource.class)
                        .path("/{roleId}")
                        .build(tenantId, createdRole.getId()) // tenantId still needed for URI building
        ).entity(createdRole).build();
    }

    @GET
    @RolesAllowed({"user", "tenant-admin", "system-admin"}) // Any authenticated user in the tenant can list roles
    public Response getRolesForTenant(@PathParam("tenantId") UUID tenantId) {
        // TenantId from path is used by TenantIdFilter to set context.
        // Service method no longer takes tenantId directly.
        List<RoleDTO> roles = roleService.getRolesByTenant();
        return Response.ok(roles).build();
    }

    @GET
    @Path("/{roleId}")
    @RolesAllowed({"user", "tenant-admin", "system-admin"}) // Any authenticated user in the tenant can get a specific role
    public Response getRoleById(@PathParam("tenantId") UUID tenantId, @PathParam("roleId") UUID roleId) {
        // TenantId from path is used by TenantIdFilter to set context.
        return roleService.getRoleById(roleId) // Service no longer takes tenantId
                .map(role -> Response.ok(role).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{roleId}")
    @RolesAllowed({"tenant-admin", "system-admin"}) // Only tenant or system admins can update roles
    public Response updateRole(@PathParam("tenantId") UUID tenantId, @PathParam("roleId") UUID roleId, @Valid RoleDTO roleDTO) {
        // TenantId from path is used by TenantIdFilter to set context.
        // Optional: Validate roleDTO.getTenantId() against context if it's set in DTO.
        if (roleDTO.getTenantId() != null && !roleDTO.getTenantId().equals(tenantId)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Tenant ID in DTO must match tenant ID in path or be null for update.\"}")
                    .build();
        }
        roleDTO.setTenantId(tenantId); // Ensure DTO has tenantId for service, if it uses it for validation against context

        return roleService.updateRole(roleId, roleDTO) // Service no longer takes tenantId
                .map(updatedRole -> Response.ok(updatedRole).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{roleId}")
    @RolesAllowed({"tenant-admin", "system-admin"}) // Only tenant or system admins can delete roles
    public Response deleteRole(@PathParam("tenantId") UUID tenantId, @PathParam("roleId") UUID roleId) {
        // TenantId from path is used by TenantIdFilter to set context.
        if (roleService.deleteRole(roleId)) { // Service no longer takes tenantId
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
        // Consider more specific error if deletion is forbidden (e.g., system role, role in use)
    }
}