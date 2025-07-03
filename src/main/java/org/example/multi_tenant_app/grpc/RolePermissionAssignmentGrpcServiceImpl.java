package org.example.multi_tenant_app.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import org.example.multi_tenant_app.grpc.role_permission.*;
import org.example.multi_tenant_app.services.RolePermissionAssignmentService;
import org.example.multi_tenant_app.web.dtos.PermissionDTO;
import org.example.multi_tenant_app.web.dtos.RolePermissionAssignmentDTO;


import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
public class RolePermissionAssignmentGrpcServiceImpl implements RolePermissionAssignmentGrpcService {

    @Inject
    RolePermissionAssignmentService service;

    // --- Conversion Utilities ---
    private RolePermissionAssignmentMessage convertAssignmentDTOToMessage(RolePermissionAssignmentDTO dto) {
        if (dto == null) {
            return RolePermissionAssignmentMessage.newBuilder().build();
        }
        RolePermissionAssignmentMessage.Builder builder = RolePermissionAssignmentMessage.newBuilder()
                .setId(dto.getId().toString())
                .setTenantId(dto.getTenantId().toString())
                .setRoleId(dto.getRoleId().toString())
                .setPermissionId(dto.getPermissionId().toString());
        if (dto.getAssignedAt() != null) {
            builder.setAssignedAt(Timestamp.newBuilder()
                    .setSeconds(dto.getAssignedAt().toEpochSecond(ZoneOffset.UTC))
                    .setNanos(dto.getAssignedAt().getNano()).build());
        }
        return builder.build();
    }

    private PermissionInfoMessage convertPermissionDTOToInfoMessage(PermissionDTO dto) {
        if (dto == null) return PermissionInfoMessage.newBuilder().build();
        PermissionInfoMessage.Builder builder = PermissionInfoMessage.newBuilder()
                .setId(dto.getId().toString())
                .setName(dto.getName());
        if (dto.getDescription() != null) {
            builder.setDescription(dto.getDescription());
        }
        if (dto.getCreatedAt() != null) {
            builder.setCreatedAt(Timestamp.newBuilder()
                    .setSeconds(dto.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                    .setNanos(dto.getCreatedAt().getNano()).build());
        }
        if (dto.getUpdatedAt() != null) {
            builder.setUpdatedAt(Timestamp.newBuilder()
                    .setSeconds(dto.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
                    .setNanos(dto.getUpdatedAt().getNano()).build());
        }
        return builder.build();
    }

    @Override
    public Uni<RolePermissionAssignmentResponse> assignPermissionToRole(AssignPermissionToRoleRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        UUID roleId = UUID.fromString(request.getRoleId());
        UUID permissionId = UUID.fromString(request.getPermissionId());
        try {
            RolePermissionAssignmentDTO dto = service.assignPermissionToRole(roleId, permissionId);
            return Uni.createFrom().item(RolePermissionAssignmentResponse.newBuilder()
                    .setAssignment(convertAssignmentDTOToMessage(dto))
                    .build());
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage())));
        } catch (Exception e) {
            return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.INTERNAL.withDescription("Failed to assign permission: " + e.getMessage())));
        }
    }

    @Override
    public Uni<Empty> removePermissionFromRole(RemovePermissionFromRoleRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        UUID roleId = UUID.fromString(request.getRoleId());
        UUID permissionId = UUID.fromString(request.getPermissionId());
        try {
            boolean removed = service.removePermissionFromRole(roleId, permissionId);
            if (removed) {
                return Uni.createFrom().item(Empty.newBuilder().build());
            } else {
                return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription("Assignment not found or role/permission mismatch.")));
            }
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage())));
        }
    }

    @Override
    public Uni<PermissionListResponse> getPermissionsForRole(GetPermissionsForRoleRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        UUID roleId = UUID.fromString(request.getRoleId());
        try {
            List<PermissionDTO> dtoList = service.getPermissionsForRole(roleId);
            List<PermissionInfoMessage> messages = dtoList.stream()
                    .map(this::convertPermissionDTOToInfoMessage)
                    .collect(Collectors.toList());
            return Uni.createFrom().item(PermissionListResponse.newBuilder().addAllPermissions(messages).build());
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage())));
        }
    }

    @Override
    public Uni<PermissionListResponse> getAllGlobalPermissions(Empty request) {
        try {
            List<PermissionDTO> dtoList = service.getAllGlobalPermissions();
            List<PermissionInfoMessage> messages = dtoList.stream()
                    .map(this::convertPermissionDTOToInfoMessage)
                    .collect(Collectors.toList());
            return Uni.createFrom().item(PermissionListResponse.newBuilder().addAllPermissions(messages).build());
        } catch (Exception e) {
            return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.INTERNAL.withDescription("Failed to retrieve global permissions: " + e.getMessage())));
        }
    }
}