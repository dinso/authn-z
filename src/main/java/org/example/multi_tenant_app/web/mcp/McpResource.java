package org.example.multi_tenant_app.web.mcp;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/mcp/v1/process") // Example path for MCP
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class McpResource {

    private static final Logger LOG = Logger.getLogger(McpResource.class);

    public static final String TENANT_ID_HEADER = "Tenant-ID"; // Standardized header name

    @POST
    public Response processMcpRequest(@HeaderParam(TENANT_ID_HEADER) String tenantId, String requestBody) {
        LOG.infof("MCP request received for Tenant-ID: %s", tenantId);

        if (tenantId == null || tenantId.trim().isEmpty()) {
            LOG.warn("MCP request received without Tenant-ID header.");
            // Returning a structured error response as per requirements
            // This should ideally use the centralized error handling mechanism (Step 6)
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"" + "error" + "\": \"" + "Missing Tenant-ID header" + "\", " +
                                   "\"" + "code" + "\": \"" + "MCP_TENANT_ID_MISSING" + "\", " +
                                   "\"" + "message" + "\": \"" + "The " + TENANT_ID_HEADER + " header is required for MCP requests." + "\"}")
                           .build();
        }

        // TODO: Implement actual MCP request processing logic
        // This logic would typically:
        // 1. Validate the tenantId (e.g., check if it's a valid UUID and corresponds to an active tenant)
        // 2. Route the request to the appropriate tenant-specific service/handler
        // 3. Process the requestBody based on the tenant's context and the MCP protocol specifics

        String responseMessage = "MCP request for tenant '" + tenantId + "' processed successfully. Body: " + requestBody;
        LOG.debugf("MCP request body for tenant %s: %s", tenantId, requestBody);

        // Placeholder response
        return Response.ok("{\"" + "message" + "\": \"" + responseMessage + "\"}").build();
    }

    @GET // Example GET endpoint for MCP, might not be typical but demonstrates header usage
    public Response getMcpInfo(@HeaderParam(TENANT_ID_HEADER) String tenantId) {
        LOG.infof("MCP GET request received for Tenant-ID: %s", tenantId);

        if (tenantId == null || tenantId.trim().isEmpty()) {
            LOG.warn("MCP GET request received without Tenant-ID header.");
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"" + "error" + "\": \"" + "Missing Tenant-ID header" + "\", " +
                                   "\"" + "code" + "\": \"" + "MCP_TENANT_ID_MISSING" + "\", " +
                                   "\"" + "message" + "\": \"" + "The " + TENANT_ID_HEADER + " header is required for MCP requests." + "\"}")
                           .build();
        }

        String responseMessage = "MCP GET info for tenant '" + tenantId + "'.";
        return Response.ok("{\"" + "message" + "\": \"" + responseMessage + "\"}").build();
    }
}
