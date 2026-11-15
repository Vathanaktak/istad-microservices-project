--liquibase formatted sql

--changeset iam:004-password-reset
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users (id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT uk_password_reset_tokens_uuid UNIQUE (uuid)
);
CREATE INDEX ix_password_reset_tokens_user ON password_reset_tokens (user_id);
CREATE INDEX ix_password_reset_tokens_hash ON password_reset_tokens (token_hash) WHERE used = FALSE;
