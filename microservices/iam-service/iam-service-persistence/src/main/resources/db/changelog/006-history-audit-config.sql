--liquibase formatted sql

--changeset iam:006-history-audit-config
CREATE TABLE login_history (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL,
    user_id BIGINT REFERENCES users (id),
    username VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    ip_address VARCHAR(50),
    location VARCHAR(100),
    user_agent VARCHAR(500),
    device VARCHAR(50),
    browser VARCHAR(50),
    os VARCHAR(50),
    suspicious BOOLEAN NOT NULL DEFAULT FALSE,
    failure_reason VARCHAR(255),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_login_history_uuid UNIQUE (uuid)
);
CREATE INDEX ix_login_history_user ON login_history (user_id);
CREATE INDEX ix_login_history_username ON login_history (username);
CREATE INDEX ix_login_history_occurred ON login_history (occurred_at);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL,
    user_id BIGINT REFERENCES users (id),
    category VARCHAR(30) NOT NULL,
    action VARCHAR(50) NOT NULL,
    outcome VARCHAR(20) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    target VARCHAR(100),
    ip_address VARCHAR(50),
    detail TEXT,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_audit_logs_uuid UNIQUE (uuid)
);
CREATE INDEX ix_audit_logs_user ON audit_logs (user_id);
CREATE INDEX ix_audit_logs_occurred ON audit_logs (occurred_at);
CREATE INDEX ix_audit_logs_category ON audit_logs (category);

CREATE TABLE deactivation_requests (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users (id),
    reason VARCHAR(50) NOT NULL,
    details TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    cancelled_at TIMESTAMPTZ,
    processed_at TIMESTAMPTZ,
    CONSTRAINT uk_deactivation_requests_uuid UNIQUE (uuid)
);
CREATE INDEX ix_deactivation_requests_user ON deactivation_requests (user_id);

CREATE TABLE system_config (
    id BIGSERIAL PRIMARY KEY,
    config_json TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by VARCHAR(100) NOT NULL DEFAULT 'system'
);
