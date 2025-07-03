package org.example.multi_tenant_app.web.controllers;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.multi_tenant_app.services.UserRoleAssignmentService;
import org.example.multi_tenant_app.web.dtos.RoleDTO;
import org.example.multi_tenant_app.web.dtos.UserRoleAssignmentDTO;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/tenants/{tenantId}/users/{userId}/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON) // Though some methods might not consume JSON
public class UserRoleAssignmentResource {

    @Inject
    UserRoleAssignmentService userRoleAssignmentService;

    // Assign a role to a user (typically roleId is in payload or as a sub-resource path)
    // Path for assigning a specific role: POST /api/v1/tenants/{tenantId}/users/{userId}/roles/{roleId}
    // Or, if roleId is in payload: POST /api/v1/tenants/{tenantId}/users/{userId}/roles
    // Let's use {roleId} as a path parameter for clarity in assignment/removal.

    @POST
    @Path("/{roleId}") // Assign a specific role
    public Response assignRoleToUser(@PathParam("tenantId") UUID tenantId,
                                     @PathParam("userId") UUID userId,
                                     @PathParam("roleId") UUID roleId) {
        try {
            UserRoleAssignmentDTO assignment = userRoleAssignmentService.assignRoleToUser(userId, roleId);
            // Consider what to return: the assignment, 201 Created, or 204 No Content if idempotent.
            // Returning the assignment details can be useful.
            return Response.status(Response.Status.CREATED).entity(assignment).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        } catch (Exception e) { // Catch other potential errors, e.g., conflict if already assigned (service might handle this)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Failed to assign role: " + e.getMessage() + "\"}").build();
        }
    }

    @DELETE
    @Path("/{roleId}") // Remove a specific role from a user
    public Response removeRoleFromUser(@PathParam("tenantId") UUID tenantId,
                                       @PathParam("userId") UUID userId,
                                       @PathParam("roleId") UUID roleId) {
        try {
            boolean removed = userRoleAssignmentService.removeRoleFromUser(userId, roleId);
            if (removed) {
                return Response.noContent().build();
            } else {
                // This could mean the user/role/assignment wasn't found, or user didn't have the role.
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"error\":\"Assignment not found or user/role mismatch.\"}").build();
            }
        } catch (NotFoundException e) { // If service throws NotFound for user/role
             return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    public Response getRolesForUser(@PathParam("tenantId") UUID tenantId,
                                    @PathParam("userId") UUID userId) {
        try {
            List<RoleDTO> roles = userRoleAssignmentService.getRolesForUser(userId);
            return Response.ok(roles).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
}
