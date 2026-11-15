--liquibase formatted sql

--changeset iam:007-deactivation-review
ALTER TABLE deactivation_requests ADD COLUMN review_notes TEXT;
ALTER TABLE deactivation_requests ADD COLUMN reviewed_by VARCHAR(100);
