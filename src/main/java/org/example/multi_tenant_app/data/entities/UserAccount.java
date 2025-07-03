package org.example.multi_tenant_app.data.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "username"}), // Username unique per tenant
    @UniqueConstraint(columnNames = {"tenant_id", "email"})    // Email unique per tenant
})
// Example of defining a Hibernate filter for multi-tenancy
// This requires further configuration in services/repositories to enable and parameterize
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class UserAccount extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "UUID")
    public UUID tenantId; // Foreign key to Tenant entity

    // Optional: If you want a direct JPA relationship to Tenant.
    // Ensure Tenant entity is correctly mapped if using this.
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "tenant_id", referencedColumnName = "id", insertable = false, updatable = false)
    // public Tenant tenant;

    @Column(name = "username", nullable = false, length = 100)
    public String username;

    @Column(name = "email", nullable = false, length = 255)
    public String email;

    // Password hash - should not be stored in plain text.
    // Actual password handling (hashing, verification) will be in the service layer.
    // This field might be omitted if identity is fully managed by an external OIDC provider.
    @Column(name = "password_hash", length = 255)
    public String passwordHash;

    @Column(name = "first_name", length = 100)
    public String firstName;

    @Column(name = "last_name", length = 100)
    public String lastName;

    @Column(name = "is_active", nullable = false)
    public boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    public UserAccount() {
    }

    public UserAccount(UUID tenantId, String username, String email) {
        this.tenantId = tenantId;
        this.username = username;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Note on @Filter:
    // To use this filter, it needs to be enabled on the Hibernate session,
    // typically in a service or repository method before executing a query.
    // Example: Panache.getEntityManager().unwrap(Session.class).enableFilter("tenantFilter").setParameter("tenantId", currentTenantId);
    // This is a common pattern for enforcing tenant isolation at the ORM level.
}
