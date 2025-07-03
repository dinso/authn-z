package org.example.multi_tenant_app.web.controllers;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.multi_tenant_app.data.entities.Tenant; // Assuming Tenant entity path
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/api/v1/tenants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated // Secure all methods in this resource
public class TenantResource {

    private static final Logger LOG = Logger.getLogger(TenantResource.class);

    // Placeholder for a service to handle business logic
    // @Inject
    // TenantService tenantService;

    @POST
    public Response createTenant(Tenant tenant) {
        // TODO: Implement tenant creation logic using TenantService
        // For now, just log and return a dummy response
        LOG.infof("Received request to create tenant: %s", tenant.name);
        tenant.id = UUID.randomUUID(); // Dummy ID
        // return Response.status(Response.Status.CREATED).entity(tenant).build();
        return Response.ok(tenant).status(Response.Status.CREATED).build(); // Placeholder
    }

    @GET
    @Path("/{id}")
    public Response getTenantById(@PathParam("id") UUID id) {
        // TODO: Implement logic to fetch tenant by ID using TenantService
        LOG.infof("Received request to get tenant by ID: %s", id);
        // Placeholder response
        Tenant tenant = new Tenant("Dummy Tenant " + id.toString(), "ACTIVE");
        tenant.id = id;
        if (tenant != null) {
            return Response.ok(tenant).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    public Response getAllTenants() {
        // TODO: Implement logic to fetch all tenants using TenantService
        LOG.info("Received request to get all tenants");
        // Placeholder response
        List<Tenant> tenants = new ArrayList<>();
        Tenant t1 = new Tenant("Tenant A", "ACTIVE");
        t1.id = UUID.randomUUID();
        Tenant t2 = new Tenant("Tenant B", "INACTIVE");
        t2.id = UUID.randomUUID();
        tenants.add(t1);
        tenants.add(t2);
        return Response.ok(tenants).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateTenant(@PathParam("id") UUID id, Tenant tenantUpdate) {
        // TODO: Implement logic to update tenant using TenantService
        LOG.infof("Received request to update tenant ID %s with data: %s", id, tenantUpdate.name);
        tenantUpdate.id = id; // Ensure ID matches path
        // Placeholder:
        return Response.ok(tenantUpdate).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteTenant(@PathParam("id") UUID id) {
        // TODO: Implement logic to delete tenant using TenantService
        LOG.infof("Received request to delete tenant ID: %s", id);
        // Placeholder:
        return Response.noContent().build();
    }
}
