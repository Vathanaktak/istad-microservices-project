--liquibase formatted sql

--changeset iam:005-locked-until
-- Timed automatic lockout: populated by failed-login throttling (not by admin lock/unlock).
ALTER TABLE users ADD COLUMN locked_until TIMESTAMPTZ;
