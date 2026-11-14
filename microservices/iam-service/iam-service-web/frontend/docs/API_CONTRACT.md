# REST endpoints used by iam-web

## Already provided by iam-service (used as-is)
`GET/PATCH /api/me`, `PUT /api/me/password`, `GET /api/me/identities`, `DELETE /api/me/identities/{id}`, `POST /api/auth/register`,
`/api/users/**` (CRUD, enable/disable, lock/unlock, password, authorities, roles, permissions, identities),
`/api/roles/**` (CRUD, activate/deactivate, permissions), `/api/permissions/**` (CRUD, activate/deactivate),
`GET /userinfo` (OIDC claims: phone, picture, nickname…).

## Not implemented in iam-service yet — iam-web falls back to demo data
The UI is complete for these; it calls the paths below. While the server answers a plain 404/405 the SPA shows a
"Sample data" banner and uses `src/api/demo.js`. Once you implement an endpoint the real response is used automatically
(set `VITE_DEMO_FALLBACK=false` to turn the fallback off). Lists use the same envelope as the existing API:
`{ "content": [...], "page": { "number", "size", "totalElements", "totalPages" } }`.

| Feature | Method + path | Response / body |
|---|---|---|
| Active sessions | `GET /api/me/sessions` | `[{ id, current, device, browser, os, ipAddress, location, createdAt, lastActiveAt }]` |
| End a session | `DELETE /api/me/sessions/{id}` | 204 |
| Sign out everywhere | `DELETE /api/me/sessions` | 204 |
| My login history | `GET /api/me/login-history?status=SUCCESS\|FAILED&page&size` | page of `{ id, status, reason, ipAddress, location, device, browser, os, occurredAt }` |
| My activity | `GET /api/me/activity?page&size` | page of `{ id, type, description, ipAddress, occurredAt }` (`PASSWORD_CHANGED`, `PROFILE_UPDATED`, `IDENTITY_LINKED`, …) |
| Deactivation | `GET /api/me/deactivation-request` → `{ status: NONE\|PENDING, requestedAt }`; `POST` body `{ reason, details }`; `DELETE` cancels | |
| Forgot / reset password | `POST /api/auth/forgot-password {email}` (202), `POST /api/auth/reset-password {token,newPassword,confirmPassword}` (204) | public |
| User sessions (admin) | `GET /api/users/{id}/sessions`, `DELETE /api/users/{id}/sessions/{sid}`, `DELETE /api/users/{id}/sessions` | authority `user:read` / `user:update` |
| User login history | `GET /api/users/{id}/login-history?status&page&size` | as "My login history" |
| Login monitoring | `GET /api/login-history?q&status&suspicious=true&page&size` | login row + `username`, `suspicious` |
| Audit logs | `GET /api/audit-logs?q&category&outcome&from&to&page&size` | page of `{ id, category: AUTH\|USER\|ROLE\|PERMISSION\|CLIENT\|SYSTEM, action, outcome: SUCCESS\|FAILURE, actor, target, ipAddress, detail, occurredAt }` — authority `audit:read` |
| OAuth2 clients | `GET/POST /api/clients`, `PUT/DELETE /api/clients/{id}`, `POST /api/clients/{id}/secret` | `{ id, clientId, clientName, authenticationMethods[none\|client_secret_basic], grantTypes[], redirectUris[], postLogoutRedirectUris[], scopes[], requireProofKey, requireAuthorizationConsent, accessTokenMinutes, refreshTokenHours, createdAt }`; create/regenerate return `clientSecret` once — authorities `client:read\|create\|update\|delete` |
| System configuration | `GET/PUT /api/system/config` | `{ authentication{allowRegistration,allowSocialLogin,maxFailedAttempts,lockoutMinutes,requireVerifiedEmail}, password{minLength,requireUppercase,requireLowercase,requireDigit,requireSymbol,expireDays,historyCount}, session{idleTimeoutMinutes,absoluteTimeoutHours,maxConcurrentSessions}, token{accessTokenMinutes,refreshTokenHours,idTokenMinutes,authorizationCodeMinutes} }` — authorities `system:read\|update` |

New authorities to seed for the ADMIN role: `client:*`, `audit:read`, `system:read`, `system:update`.
The ADMIN role always sees every admin page in the UI; other roles see a page when their token has the matching `*:read` permission.

## Known gaps in the existing API (UI works around them)
* `GET /api/me` does not return phone / picture / nickname → read from `/userinfo`.
* `PATCH /api/me` cannot change username or email → shown read-only. Profile picture is a URL (max 500 chars), not an upload.
* `GET /api/users/{id}` has no `locked` flag → looked up from the list endpoint.
