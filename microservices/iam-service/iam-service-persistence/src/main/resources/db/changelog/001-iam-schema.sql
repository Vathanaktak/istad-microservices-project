--liquibase formatted sql

--changeset iam:001-users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    password_hash VARCHAR(255),
    given_name VARCHAR(100) NOT NULL,
    family_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    nickname VARCHAR(100),
    gender VARCHAR(20),
    birthdate DATE,
    profile_url VARCHAR(500),
    picture_url VARCHAR(500),
    website_url VARCHAR(500),
    zoneinfo VARCHAR(50),
    locale VARCHAR(10) NOT NULL DEFAULT 'en',
    phone_number VARCHAR(20),
    phone_number_verified BOOLEAN NOT NULL DEFAULT FALSE,
    address_street_address VARCHAR(255),
    address_locality VARCHAR(100),
    address_region VARCHAR(100),
    address_postal_code VARCHAR(20),
    address_country CHAR(2),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_login_at TIMESTAMPTZ,
    password_changed_at TIMESTAMPTZ,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_users_uuid UNIQUE (uuid)
);
-- Unique among live rows only, so soft-deleted users free their username/email/phone.
CREATE UNIQUE INDEX uk_users_username ON users (lower(username)) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX uk_users_email ON users (email) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX uk_users_phone ON users (phone_number) WHERE is_deleted = FALSE AND phone_number IS NOT NULL;

--changeset iam:002-roles-permissions
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_roles_uuid UNIQUE (uuid)
);
CREATE UNIQUE INDEX uk_roles_name ON roles (name) WHERE is_deleted = FALSE;

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_permissions_uuid UNIQUE (uuid)
);
CREATE UNIQUE INDEX uk_permissions_name ON permissions (name) WHERE is_deleted = FALSE;

--changeset iam:003-identities
CREATE TABLE identities (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users (id),
    provider VARCHAR(20) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    display_name VARCHAR(150),
    username VARCHAR(100),
    picture_url VARCHAR(500),
    attributes JSONB,
    linked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login_at TIMESTAMPTZ,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_identities_uuid UNIQUE (uuid),
    CONSTRAINT ck_identities_provider CHECK (provider IN ('GOOGLE','GITHUB','FACEBOOK','TELEGRAM'))
);
CREATE UNIQUE INDEX uk_identities_provider_subject ON identities (provider, provider_subject) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX uk_identities_user_provider ON identities (user_id, provider) WHERE is_deleted = FALSE;
CREATE INDEX ix_identities_user ON identities (user_id);

--changeset iam:004-assignments
CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    role_id BIGINT NOT NULL REFERENCES roles (id),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    assigned_by VARCHAR(100) NOT NULL,
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    revoked_by VARCHAR(100),
    reason VARCHAR(255)
);
CREATE UNIQUE INDEX uk_user_roles_active ON user_roles (user_id, role_id) WHERE revoked_at IS NULL;
CREATE INDEX ix_user_roles_user ON user_roles (user_id);

CREATE TABLE role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles (id),
    permission_id BIGINT NOT NULL REFERENCES permissions (id),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    assigned_by VARCHAR(100) NOT NULL,
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    revoked_by VARCHAR(100),
    reason VARCHAR(255)
);
CREATE UNIQUE INDEX uk_role_permissions_active ON role_permissions (role_id, permission_id) WHERE revoked_at IS NULL;
CREATE INDEX ix_role_permissions_role ON role_permissions (role_id);

CREATE TABLE user_permissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    permission_id BIGINT NOT NULL REFERENCES permissions (id),
    effect VARCHAR(5) NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    assigned_by VARCHAR(100) NOT NULL,
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    revoked_by VARCHAR(100),
    reason VARCHAR(255),
    CONSTRAINT ck_user_permissions_effect CHECK (effect IN ('GRANT','DENY'))
);
CREATE UNIQUE INDEX uk_user_permissions_active ON user_permissions (user_id, permission_id) WHERE revoked_at IS NULL;
CREATE INDEX ix_user_permissions_user ON user_permissions (user_id);
