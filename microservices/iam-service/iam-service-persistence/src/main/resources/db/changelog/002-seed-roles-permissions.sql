--liquibase formatted sql

--changeset iam:010-seed-permissions
INSERT INTO permissions (uuid, name, resource, action, description, is_system, is_active, created_by, updated_by)
SELECT gen_random_uuid(), r.res || ':' || a.act, r.res, a.act, a.act || ' ' || r.res, TRUE, TRUE, 'system', 'system'
FROM (VALUES ('user'), ('role'), ('permission')) AS r(res)
CROSS JOIN (VALUES ('create'), ('read'), ('update'), ('delete'), ('assign')) AS a(act)
WHERE NOT (r.res = 'permission' AND a.act = 'assign');

--changeset iam:011-seed-roles
INSERT INTO roles (uuid, name, description, is_system, is_active, created_by, updated_by) VALUES
 (gen_random_uuid(), 'ADMIN', 'Full administrative access', TRUE, TRUE, 'system', 'system'),
 (gen_random_uuid(), 'USER',  'Default role for every registered user', TRUE, TRUE, 'system', 'system');

--changeset iam:012-seed-role-permissions
INSERT INTO role_permissions (role_id, permission_id, assigned_by, reason)
SELECT r.id, p.id, 'system', 'Seed' FROM roles r CROSS JOIN permissions p WHERE r.name = 'ADMIN';
INSERT INTO role_permissions (role_id, permission_id, assigned_by, reason)
SELECT r.id, p.id, 'system', 'Seed' FROM roles r CROSS JOIN permissions p
WHERE r.name = 'USER' AND p.name IN ('user:read', 'role:read', 'permission:read');
