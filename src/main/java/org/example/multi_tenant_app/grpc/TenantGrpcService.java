package org.example.multi_tenant_app.grpc;

import io.quarkus.grpc.GrpcService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

// Explicit imports for gRPC tenant service classes
import org.example.multi_tenant_app.grpc.tenant.Tenant;
import org.example.multi_tenant_app.grpc.tenant.TenantService;
import org.example.multi_tenant_app.grpc.tenant.CreateTenantRequest;
import org.example.multi_tenant_app.grpc.tenant.CreateTenantResponse;
import org.example.multi_tenant_app.grpc.tenant.GetTenantRequest;
import org.example.multi_tenant_app.grpc.tenant.ListTenantsRequest;
import org.example.multi_tenant_app.grpc.tenant.ListTenantsResponse;
import org.example.multi_tenant_app.grpc.tenant.UpdateTenantRequest;
import org.example.multi_tenant_app.grpc.tenant.UpdateTenantResponse;
import org.example.multi_tenant_app.grpc.tenant.DeleteTenantRequest;

import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.protobuf.Empty;

@GrpcService
public class TenantGrpcService implements TenantService {

    private static final Logger LOG = Logger.getLogger(TenantGrpcService.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    // Placeholder for a real service/repository
    // @Inject
    // ActualTenantService actualTenantService;

    @Override
    @Blocking // Use @Blocking if the underlying service call is blocking
    public Uni<CreateTenantResponse> createTenant(CreateTenantRequest request) {
        LOG.infof("gRPC CreateTenant called for name: %s", request.getName());
        if (request.getName() == null || request.getName().isBlank()) {
            return Uni.createFrom().failure(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Tenant name cannot be empty.")));
        }
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            // Default status or require it explicitly
            return Uni.createFrom().failure(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Tenant status cannot be empty.")));
        }

        // TODO: Integrate with actual tenant creation logic
        Tenant tenantProto = Tenant.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName(request.getName())
                .setStatus(request.getStatus())
                .setCreatedAt(LocalDateTime.now().format(ISO_FORMATTER))
                .setUpdatedAt(LocalDateTime.now().format(ISO_FORMATTER))
                .build();
        return Uni.createFrom().item(CreateTenantResponse.newBuilder().setTenant(tenantProto).build());
    }

    @Override
    @Blocking
    public Uni<Tenant> getTenant(GetTenantRequest request) {
        LOG.infof("gRPC GetTenant called for ID: %s", request.getId());
        if (request.getId() == null || request.getId().isBlank()) {
            return Uni.createFrom().failure(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Tenant ID cannot be empty.")));
        }
        try {
            UUID.fromString(request.getId()); // Validate if ID is a valid UUID
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Invalid Tenant ID format.")));
        }

        // TODO: Integrate with actual tenant retrieval logic
        // TODO: Add logic to return NOT_FOUND if tenant does not exist
        Tenant tenantProto = Tenant.newBuilder()
                .setId(request.getId())
                .setName("Dummy Tenant GRPC " + request.getId())
                .setStatus("ACTIVE")
                .setCreatedAt(LocalDateTime.now().minusDays(1).format(ISO_FORMATTER))
                .setUpdatedAt(LocalDateTime.now().format(ISO_FORMATTER))
                .build();
        return Uni.createFrom().item(tenantProto);
    }

    @Override
    @Blocking
    public Uni<ListTenantsResponse> listTenants(ListTenantsRequest request) {
        LOG.info("gRPC ListTenants called");
        // TODO: Integrate with actual tenant listing logic
        List<Tenant> tenantProtos = new ArrayList<>();
        tenantProtos.add(Tenant.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("Tenant G-A")
                .setStatus("ACTIVE")
                .setCreatedAt(LocalDateTime.now().minusHours(5).format(ISO_FORMATTER))
                .setUpdatedAt(LocalDateTime.now().minusHours(1).format(ISO_FORMATTER))
                .build());
        tenantProtos.add(Tenant.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("Tenant G-B")
                .setStatus("INACTIVE")
                .setCreatedAt(LocalDateTime.now().minusDays(2).format(ISO_FORMATTER))
                .setUpdatedAt(LocalDateTime.now().minusDays(1).format(ISO_FORMATTER))
                .build());
        return Uni.createFrom().item(ListTenantsResponse.newBuilder().addAllTenants(tenantProtos).build());
    }

    @Override
    @Blocking
    public Uni<UpdateTenantResponse> updateTenant(UpdateTenantRequest request) {
        LOG.infof("gRPC UpdateTenant called for ID: %s", request.getId());
        if (request.getId() == null || request.getId().isBlank()) {
            return Uni.createFrom().failure(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Tenant ID cannot be empty for update.")));
        }
        try {
            UUID.fromString(request.getId()); // Validate if ID is a valid UUID
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Invalid Tenant ID format for update.")));
        }
        if ((request.getName() == null || request.getName().isBlank()) &&
            (request.getStatus() == null || request.getStatus().isBlank())) {
            return Uni.createFrom().failure(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("At least one field (name or status) must be provided for update.")));
        }

        // TODO: Integrate with actual tenant update logic
        // TODO: Add logic to return NOT_FOUND if tenant does not exist
        Tenant tenantProto = Tenant.newBuilder()
                .setId(request.getId())
                .setName(request.getName())
                .setStatus(request.getStatus())
                .setCreatedAt(LocalDateTime.now().minusDays(1).format(ISO_FORMATTER)) // Assuming created_at doesn't change
                .setUpdatedAt(LocalDateTime.now().format(ISO_FORMATTER))
                .build();
        return Uni.createFrom().item(UpdateTenantResponse.newBuilder().setTenant(tenantProto).build());
    }

    @Override
    @Blocking
    public Uni<Empty> deleteTenant(DeleteTenantRequest request) {
        LOG.infof("gRPC DeleteTenant called for ID: %s", request.getId());
        if (request.getId() == null || request.getId().isBlank()) {
            return Uni.createFrom().failure(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Tenant ID cannot be empty for deletion.")));
        }
        try {
            UUID.fromString(request.getId()); // Validate if ID is a valid UUID
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Invalid Tenant ID format for deletion.")));
        }

        // TODO: Integrate with actual tenant deletion logic
        // TODO: Consider if NOT_FOUND should be returned if tenant does not exist, or if delete is idempotent
        return Uni.createFrom().item(Empty.newBuilder().build());
    }
}
