package org.example.multi_tenant_app.web.controllers;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.multi_tenant_app.services.RolePermissionAssignmentService;
import org.example.multi_tenant_app.web.dtos.PermissionDTO;
import org.example.multi_tenant_app.web.dtos.RolePermissionAssignmentDTO;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/tenants/{tenantId}/roles/{roleId}/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON) // Some methods might not consume JSON (e.g., DELETE)
public class RolePermissionAssignmentResource {

    @Inject
    RolePermissionAssignmentService rolePermissionAssignmentService;

    @POST
    @Path("/{permissionId}") // Assign a specific permission to a role
    public Response assignPermissionToRole(@PathParam("tenantId") UUID tenantId,
                                           @PathParam("roleId") UUID roleId,
                                           @PathParam("permissionId") UUID permissionId) {
        try {
            RolePermissionAssignmentDTO assignment = rolePermissionAssignmentService.assignPermissionToRole(roleId, permissionId);
            return Response.status(Response.Status.CREATED).entity(assignment).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        } catch (Exception e) { // Catch other potential errors (e.g., system role modification attempt)
            // Consider specific exceptions for forbidden operations
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Failed to assign permission: " + e.getMessage() + "\"}").build();
        }
    }

    @DELETE
    @Path("/{permissionId}") // Remove a specific permission from a role
    public Response removePermissionFromRole(@PathParam("tenantId") UUID tenantId,
                                             @PathParam("roleId") UUID roleId,
                                             @PathParam("permissionId") UUID permissionId) {
        try {
            boolean removed = rolePermissionAssignmentService.removePermissionFromRole(roleId, permissionId);
            if (removed) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"error\":\"Assignment not found or role/permission mismatch.\"}").build();
            }
        } catch (NotFoundException e) {
             return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    public Response getPermissionsForRole(@PathParam("tenantId") UUID tenantId,
                                          @PathParam("roleId") UUID roleId) {
        try {
            List<PermissionDTO> permissions = rolePermissionAssignmentService.getPermissionsForRole(roleId);
            return Response.ok(permissions).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    // Optional: Endpoint to list all available global permissions (could be in a separate PermissionResource)
    @GET
    @Path("/available") // Sibling path to avoid conflict with {permissionId}
    // This path might be better as /api/v1/permissions if it's truly global and not tenant/role specific context
    // For now, placing it here for convenience of this resource.
    public Response getAllGlobalPermissions(@PathParam("tenantId") UUID tenantId, @PathParam("roleId") UUID roleId) {
        // tenantId and roleId are in path but not strictly needed for this global listing
        // They are included to fit the resource path structure, but service method is global.
        List<PermissionDTO> permissions = rolePermissionAssignmentService.getAllGlobalPermissions();
        return Response.ok(permissions).build();
    }
}
