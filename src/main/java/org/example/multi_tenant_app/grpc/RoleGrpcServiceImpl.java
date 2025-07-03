package org.example.multi_tenant_app.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.example.multi_tenant_app.grpc.role.*;
import org.example.multi_tenant_app.services.RoleService;
import org.example.multi_tenant_app.web.dtos.RoleDTO;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
public class RoleGrpcServiceImpl implements RoleGrpcService {

    @Inject
    RoleService roleService; // The existing service for business logic

    // --- Conversion Utilities ---
    private RoleMessage convertRoleDTOToMessage(RoleDTO dto) {
        if (dto == null) {
            return RoleMessage.newBuilder().build(); // Or throw error
        }
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

    private RoleDTO convertCreateRequestToDTO(CreateRoleRequest request) {
        RoleDTO dto = new RoleDTO();
        dto.setTenantId(UUID.fromString(request.getTenantId()));
        dto.setName(request.getName());
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            dto.setDescription(request.getDescription());
        }
        // isSystemRole is false by default in DTO, which is correct for client-created roles
        return dto;
    }

    private RoleDTO convertUpdateRequestToDTO(UpdateRoleRequest request) {
        RoleDTO dto = new RoleDTO();
        // tenantId and roleId are path params in REST, here they are part of request
        dto.setTenantId(UUID.fromString(request.getTenantId()));
        dto.setId(UUID.fromString(request.getRoleId()));
        dto.setName(request.getName());
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            dto.setDescription(request.getDescription());
        }
        return dto;
    }


    @Override
    public Uni<RoleResponse> createRole(CreateRoleRequest request) {
        RoleDTO dtoToCreate = convertCreateRequestToDTO(request);
        RoleDTO createdRole = roleService.createRole(UUID.fromString(request.getTenantId()), dtoToCreate);
        return Uni.createFrom().item(RoleResponse.newBuilder().setRole(convertRoleDTOToMessage(createdRole)).build());
    }

    @Override
    public Uni<RoleResponse> getRole(GetRoleRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        UUID roleId = UUID.fromString(request.getRoleId());
        return Uni.createFrom().optional(roleService.getRoleById(tenantId, roleId))
                .map(roleDTO -> RoleResponse.newBuilder().setRole(convertRoleDTOToMessage(roleDTO)).build())
                .onFailure().transform(err -> new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription(err.getMessage())));
    }

    @Override
    public Uni<RoleListResponse> getRolesForTenant(GetRolesForTenantRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        List<RoleDTO> dtoList = roleService.getRolesByTenant(tenantId);
        List<RoleMessage> messages = dtoList.stream()
                                            .map(this::convertRoleDTOToMessage)
                                            .collect(Collectors.toList());
        return Uni.createFrom().item(RoleListResponse.newBuilder().addAllRoles(messages).build());
    }

    @Override
    public Uni<RoleResponse> updateRole(UpdateRoleRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        UUID roleId = UUID.fromString(request.getRoleId());
        RoleDTO dtoToUpdate = convertUpdateRequestToDTO(request);

        return Uni.createFrom().optional(roleService.updateRole(tenantId, roleId, dtoToUpdate))
                .map(updatedRoleDTO -> RoleResponse.newBuilder().setRole(convertRoleDTOToMessage(updatedRoleDTO)).build())
                .onFailure().transform(err -> new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription(err.getMessage())));
    }

    @Override
    public Uni<Empty> deleteRole(DeleteRoleRequest request) {
        UUID tenantId = UUID.fromString(request.getTenantId());
        UUID roleId = UUID.fromString(request.getRoleId());
        boolean deleted = roleService.deleteRole(tenantId, roleId);
        if (deleted) {
            return Uni.createFrom().item(Empty.newBuilder().build());
        } else {
            // This could be NOT_FOUND or PERMISSION_DENIED if trying to delete a system role
            // RoleService currently returns false for system role deletion attempt without throwing specific error
            return Uni.createFrom().failure(new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND.withDescription("Role not found or could not be deleted.")));
        }
    }
}
