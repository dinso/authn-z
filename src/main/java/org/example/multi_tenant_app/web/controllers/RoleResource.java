package org.example.multi_tenant_app.web.controllers;

import org.example.multi_tenant_app.services.RoleService;
import org.example.multi_tenant_app.web.dtos.RoleDTO;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/tenants/{tenantId}/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleResource {

    @Inject
    RoleService roleService;

    @POST
    public Response createRole(@PathParam("tenantId") UUID tenantId, @Valid RoleDTO roleDTO) {
        // Ensure the tenantId in the path is used, or validate against roleDTO.getTenantId()
        if (roleDTO.getTenantId() == null) {
            roleDTO.setTenantId(tenantId);
        } else if (!tenantId.equals(roleDTO.getTenantId())) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Tenant ID in path does not match tenant ID in body.\"}")
                           .build();
        }

        RoleDTO createdRole = roleService.createRole(tenantId, roleDTO);
        return Response.created(
            UriBuilder.fromResource(RoleResource.class)
                      .path("/{roleId}")
                      .build(tenantId, createdRole.getId())
        ).entity(createdRole).build();
    }

    @GET
    public Response getRolesForTenant(@PathParam("tenantId") UUID tenantId) {
        List<RoleDTO> roles = roleService.getRolesByTenant(tenantId);
        return Response.ok(roles).build();
    }

    @GET
    @Path("/{roleId}")
    public Response getRoleById(@PathParam("tenantId") UUID tenantId, @PathParam("roleId") UUID roleId) {
        return roleService.getRoleById(tenantId, roleId)
                .map(role -> Response.ok(role).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{roleId}")
    public Response updateRole(@PathParam("tenantId") UUID tenantId, @PathParam("roleId") UUID roleId, @Valid RoleDTO roleDTO) {
        if (roleDTO.getTenantId() == null) {
            roleDTO.setTenantId(tenantId);
        } else if (!tenantId.equals(roleDTO.getTenantId())) {
             return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Tenant ID in path does not match tenant ID in body for update.\"}")
                           .build();
        }

        return roleService.updateRole(tenantId, roleId, roleDTO)
                .map(updatedRole -> Response.ok(updatedRole).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{roleId}")
    public Response deleteRole(@PathParam("tenantId") UUID tenantId, @PathParam("roleId") UUID roleId) {
        if (roleService.deleteRole(tenantId, roleId)) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
        // Consider more specific error if deletion is forbidden (e.g., system role, role in use)
    }
}
