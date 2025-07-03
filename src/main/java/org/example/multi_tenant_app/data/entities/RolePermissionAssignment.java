package org.example.multi_tenant_app.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "role_permission_assignments", uniqueConstraints = {
    // A role can only have a specific permission assigned once (within the context of the role's tenant)
    @UniqueConstraint(columnNames = {"tenant_id", "role_id", "permission_id"})
})
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class RolePermissionAssignment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    public UUID tenantId; // Denormalized for easier filtering, corresponds to Role's tenantId

    @Column(name = "role_id", nullable = false, columnDefinition = "UUID")
    public UUID roleId;

    @Column(name = "permission_id", nullable = false, columnDefinition = "UUID")
    public UUID permissionId;

    // Optional: Direct JPA relationships
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false)
    // public Role role;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "permission_id", referencedColumnName = "id", insertable = false, updatable = false)
    // public Permission permission;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "tenant_id", referencedColumnName = "id", insertable = false, updatable = false)
    // public Tenant tenant; // Could link to Tenant if needed directly

    @Column(name = "assigned_at", nullable = false, updatable = false)
    public LocalDateTime assignedAt;

    public RolePermissionAssignment() {
    }

    public RolePermissionAssignment(UUID tenantId, UUID roleId, UUID permissionId) {
        this.tenantId = tenantId; // This should be the tenantId of the Role
        this.roleId = roleId;
        this.permissionId = permissionId;
        this.assignedAt = LocalDateTime.now();
    }
}
