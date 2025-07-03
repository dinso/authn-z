package org.example.multi_tenant_app.security;

import io.grpc.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * A gRPC ServerInterceptor to extract Tenant ID from metadata and populate TenantContext.
 */
@ApplicationScoped // Quarkus uses this to make it a CDI bean that can be discovered as an interceptor
public class GrpcTenantIdInterceptor implements ServerInterceptor {

    @Inject
    TenantContext tenantContext;

    // Define the metadata key for Tenant ID
    public static final Metadata.Key<String> TENANT_ID_METADATA_KEY =
            Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            final Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String tenantIdStr = headers.get(TENANT_ID_METADATA_KEY);
        UUID tenantId = null;

        if (tenantIdStr != null && !tenantIdStr.isBlank()) {
            try {
                tenantId = UUID.fromString(tenantIdStr);
                tenantContext.setCurrentTenantId(tenantId);
                System.out.println("GrpcTenantIdInterceptor: TenantContext populated with Tenant ID: " + tenantId + " for method: " + call.getMethodDescriptor().getFullMethodName());
            } catch (IllegalArgumentException e) {
                System.err.println("GrpcTenantIdInterceptor: Invalid Tenant ID format in metadata: " + tenantIdStr);
                // Terminate call with an error
                call.close(Status.INVALID_ARGUMENT.withDescription("Invalid Tenant ID format in metadata: " + tenantIdStr), new Metadata());
                return new ServerCall.Listener<ReqT>() {
                    // No-op listener implementation
                };
            }
        } else {
            // No tenant ID found in metadata.
            // Behavior depends on requirements. For gRPC, often tenant ID is expected.
            // If strictly required for all/most gRPC calls, could close call with FAILED_PRECONDITION or UNAUTHENTICATED.
            // For now, allowing call to proceed. Services must check TenantContext.getRequiredTenantId().
            System.out.println("GrpcTenantIdInterceptor: No Tenant ID found in metadata for method: " + call.getMethodDescriptor().getFullMethodName());
        }

        // It's important to clear the TenantContext after the call finishes to avoid leakage in pooled threads.
        // However, TenantContext is @RequestScoped, so it should be cleaned up by CDI automatically at the end of the "request" (gRPC call).
        // If TenantContext were @ApplicationScoped or other broader scopes, manual cleanup in a try-finally or similar would be critical.

        return next.startCall(call, headers);
    }
}
