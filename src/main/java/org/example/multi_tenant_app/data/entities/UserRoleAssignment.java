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
@Table(name = "user_role_assignments", uniqueConstraints = {
    // A user can only have a specific role assigned once within a tenant
    @UniqueConstraint(columnNames = {"tenant_id", "user_account_id", "role_id"})
})
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class UserRoleAssignment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    public UUID tenantId; // To directly associate assignment with a tenant

    @Column(name = "user_account_id", nullable = false, columnDefinition = "UUID")
    public UUID userAccountId;

    @Column(name = "role_id", nullable = false, columnDefinition = "UUID")
    public UUID roleId;

    // Optional: Direct JPA relationships if complex queries/eager fetching is needed often
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "user_account_id", referencedColumnName = "id", insertable = false, updatable = false)
    // public UserAccount userAccount;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false)
    // public Role role;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "tenant_id", referencedColumnName = "id", insertable = false, updatable = false)
    // public Tenant tenant;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    public LocalDateTime assignedAt;

    public UserRoleAssignment() {
    }

    public UserRoleAssignment(UUID tenantId, UUID userAccountId, UUID roleId) {
        this.tenantId = tenantId;
        this.userAccountId = userAccountId;
        this.roleId = roleId;
        this.assignedAt = LocalDateTime.now();
    }
}
