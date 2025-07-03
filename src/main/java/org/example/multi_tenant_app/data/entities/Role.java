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
@Table(name = "roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "name"}) // Role name unique per tenant
})
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Role extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    public UUID tenantId;

    // Optional: Direct JPA relationship to Tenant
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "tenant_id", referencedColumnName = "id", insertable = false, updatable = false)
    // public Tenant tenant;

    @Column(name = "name", nullable = false, length = 100)
    public String name;

    @Column(name = "description", length = 255)
    public String description;

    @Column(name = "is_system_role", nullable = false)
    public boolean isSystemRole = false; // Indicates if the role is a system-defined role (not deletable by tenant admins)

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    public Role() {
    }

    public Role(UUID tenantId, String name, String description, boolean isSystemRole) {
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        this.isSystemRole = isSystemRole;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
