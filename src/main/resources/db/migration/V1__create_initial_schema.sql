-- V1: Create initial schema for tenants, users, roles, and permissions

-- Tenants Table
CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- User Accounts Table
-- Usernames and emails should be unique per tenant.
CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255), -- Nullable if using OIDC primarily
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (tenant_id, username),
    UNIQUE (tenant_id, email)
);
CREATE INDEX idx_user_accounts_tenant_id ON user_accounts(tenant_id);

-- Roles Table
-- Role names should be unique per tenant.
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (tenant_id, name)
);
CREATE INDEX idx_roles_tenant_id ON roles(tenant_id);

-- Permissions Table
-- Permissions are global and pre-defined in the system.
CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE, -- e.g., "user:create", "document:read"
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- User Role Assignments (Many-to-Many between UserAccounts and Roles)
CREATE TABLE user_role_assignments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE, -- For direct filtering and data integrity
    user_account_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP NOT NULL,
    UNIQUE (tenant_id, user_account_id, role_id)
);
CREATE INDEX idx_user_role_assignments_tenant_id ON user_role_assignments(tenant_id);
CREATE INDEX idx_user_role_assignments_user_id ON user_role_assignments(user_account_id);
CREATE INDEX idx_user_role_assignments_role_id ON user_role_assignments(role_id);


-- Role Permission Assignments (Many-to-Many between Roles and Permissions)
CREATE TABLE role_permission_assignments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE, -- Corresponds to the tenant_id of the role_id
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP NOT NULL,
    UNIQUE (tenant_id, role_id, permission_id)
);
CREATE INDEX idx_role_permission_assignments_tenant_id ON role_permission_assignments(tenant_id);
CREATE INDEX idx_role_permission_assignments_role_id ON role_permission_assignments(role_id);
CREATE INDEX idx_role_permission_assignments_permission_id ON role_permission_assignments(permission_id);

-- Seed some global permissions (examples)
-- These UUIDs are placeholders; in a real system, they might be predefined constants or generated consistently.
INSERT INTO permissions (id, name, description, created_at, updated_at) VALUES
    (RANDOM_UUID(), 'tenant:manage_settings', 'Manage tenant-level settings', NOW(), NOW()),
    (RANDOM_UUID(), 'user:create', 'Create users within the tenant', NOW(), NOW()),
    (RANDOM_UUID(), 'user:read', 'Read user information within the tenant', NOW(), NOW()),
    (RANDOM_UUID(), 'user:update', 'Update users within the tenant', NOW(), NOW()),
    (RANDOM_UUID(), 'user:delete', 'Delete users within the tenant', NOW(), NOW()),
    (RANDOM_UUID(), 'user:manage_roles', 'Assign/unassign roles to users', NOW(), NOW()),
    (RANDOM_UUID(), 'role:create', 'Create roles within the tenant', NOW(), NOW()),
    (RANDOM_UUID(), 'role:read', 'Read roles within the tenant', NOW(), NOW()),
    (RANDOM_UUID(), 'role:update', 'Update roles within the tenant', NOW(), NOW()),
    (RANDOM_UUID(), 'role:delete', 'Delete roles within the tenant', NOW(), NOW()),
    (RANDOM_UUID(), 'role:manage_permissions', 'Assign/unassign permissions to roles', NOW(), NOW());
