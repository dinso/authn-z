package org.example.multi_tenant_app.grpc;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import org.example.multi_tenant_app.grpc.health.HealthCheckRequest;
import org.example.multi_tenant_app.grpc.health.HealthCheckResponse;
import org.example.multi_tenant_app.grpc.health.HealthCheckService;

@GrpcService
public class HealthCheckServiceImpl implements HealthCheckService {

    @Override
    public Uni<HealthCheckResponse> check(HealthCheckRequest request) {
        // For now, always return SERVING.
        // In a real application, this would involve checking database connections,
        // downstream services, etc.
        // If request.getService() is not empty, specific checks for that service could be done.
        HealthCheckResponse.ServingStatus status = HealthCheckResponse.ServingStatus.SERVING;

        return Uni.createFrom().item(
                HealthCheckResponse.newBuilder().setStatus(status).build());
    }
}
