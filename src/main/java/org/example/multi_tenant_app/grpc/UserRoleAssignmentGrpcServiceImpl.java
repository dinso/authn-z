package org.example.multi_tenant_app.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import org.example.multi_tenant_app.grpc.role.RoleMessage; // Reusing from role service proto
import org.example.multi_tenant_app.grpc.user_role.*;
import org.example.multi_tenant_app.services.UserRoleAssignmentService;
import org.example.multi_tenant_app.web.dtos.RoleDTO;
import org.example.multi_tenant_app.web.dtos.UserRoleAssignmentDTO;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
public class UserRoleAssignmentGrpcServiceImpl implements UserRoleAssignmentGrpcService {

    @Inject
    UserRoleAssignmentService userRoleAssignmentService;

    // --- Conversion Utilities ---
    private UserRoleAssignmentMessage convertAssignmentDTOToMessage(UserRoleAssignmentDTO dto) {
        if (dto == null) {
            return UserRoleAssignmentMessage.newBuilder().build();
        }
        UserRoleAssignmentMessage.Builder builder = UserRoleAssignmentMessage.newBuilder()
                .setId(dto.getId().toString())
                .setTenantId(dto.getTenantId().toString())
                .setUserAccountId(dto.getUserAccountId().toString())
                .setRoleId(dto.getRoleId().toString());
        if (dto.getAssignedAt() != null) {
            builder.setAssignedAt(Timestamp.newBuilder()
                    .setSeconds(dto.getAssignedAt().toEpochSecond(ZoneOffset.UTC))
                    .setNanos(dto.getAssignedAt().getNano()).build());
        }
        return builder.build();
    }

    // Helper to convert RoleDTO to RoleMessage (similar to one in RoleGrpcServiceImpl)
    // This could be moved to a common gRPC utility class if it grows.
    private RoleMessage convertRoleDTOToRoleMessage(RoleDTO dto) {
        if (dto == null) return RoleMessage.newBuilder().build();
        RoleMessage.Builder builder = RoleMessage.newBuilder()
                .setId(dto.getId().toString())
                .setTenantId(dto.getTenantId().toString())
                .setName(dto.getName())
                .setIsSystemRole(dto.isSystemRole());
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
    public Uni<UserRoleAssignmentResponse> assignRoleToUser(AssignRoleToUserRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        UUID userId = UUID.fromString(request.getUserAccountId());
        UUID roleId = UUID.fromString(request.getRoleId());

        try {
            UserRoleAssignmentDTO assignmentDTO = userRoleAssignmentService.assignRoleToUser(tenantId, userId, roleId);
            return Uni.createFrom().item(UserRoleAssignmentResponse.newBuilder()
                    .setAssignment(convertAssignmentDTOToMessage(assignmentDTO))
                    .build());
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage())));
        } catch (Exception e) { // Catch other potential errors
            return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.INTERNAL.withDescription("Failed to assign role: " + e.getMessage())));
        }
    }

    @Override
    public Uni<Empty> removeRoleFromUser(RemoveRoleFromUserRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        UUID userId = UUID.fromString(request.getUserAccountId());
        UUID roleId = UUID.fromString(request.getRoleId());

        try {
            boolean removed = userRoleAssignmentService.removeRoleFromUser(tenantId, userId, roleId);
            if (removed) {
                return Uni.createFrom().item(Empty.newBuilder().build());
            } else {
                return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription("Assignment not found or user/role mismatch.")));
            }
        } catch (jakarta.ws.rs.NotFoundException e) {
             return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage())));
        }
    }

    @Override
    public Uni<UserRolesListResponse> getRolesForUser(GetRolesForUserRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        UUID userId = UUID.fromString(request.getUserAccountId());

        try {
            List<RoleDTO> roleDTOs = userRoleAssignmentService.getRolesForUser(tenantId, userId);
            List<RoleMessage> roleMessages = roleDTOs.stream()
                    .map(this::convertRoleDTOToRoleMessage)
                    .collect(Collectors.toList());
            return Uni.createFrom().item(UserRolesListResponse.newBuilder().addAllRoles(roleMessages).build());
        } catch (jakarta.ws.rs.NotFoundException e) {
             return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage())));
        }
    }
}
