# iam-web — React frontend for the ISTAD IAM platform

React 18 + Vite + React Router. English and Khmer (language switch, remembered in `localStorage`).
Replaces the old JTE module: the sign-in, register and consent screens are React too.

## How it talks to iam-service
* **Auth**: OAuth2 Authorization Code + **PKCE** with the public client `iam-web` (seeded by `ClientSeeder`). Tokens live in
  `sessionStorage`; the access token is refreshed with the rotating refresh token. Roles/permissions come from the access-token claims.
* **Sign-in page**: a real `<form method=post action=/login>` to Spring Security (keeps the IAM browser session needed by
  `/oauth2/authorize`). CSRF is a double-submit `XSRF-TOKEN` cookie. Social login = links to `/oauth2/authorization/{google|github|facebook}`.
* **API**: `Authorization: Bearer <access token>` on `/api/**`; RFC 9457 problem responses are mapped to field errors and toasts.

## Run it
```bash
# 1) backend (from the repo root) — http://localhost:9000
docker compose up -d && java -jar iam-service-main/build/libs/iam-service-main-*.jar

# 2a) development with hot reload — http://localhost:5173 (Vite proxies the backend, so it is same-origin)
cd iam-service-web/frontend && npm install && npm run dev

# 2b) or served by Spring Boot — ./gradlew :iam-service-main:build builds the frontend into the jar (-PskipFrontend to skip)
```
Default admin: `IAM_ADMIN_USERNAME` / `IAM_ADMIN_PASSWORD` (see `application.yml`).

## Feature map (assignment → page)
| Requirement | Route |
|---|---|
| Profile · Security & password · Linked accounts | `/account/profile` · `/account/security` · `/account/linked` |
| Active sessions + session management (end one / all) | `/account/sessions` |
| Login history · Account activity · Deactivation | `/account/login-history` · `/account/activity` · `/account/deactivation` |
| Admin overview | `/admin` |
| Users, role assignment, permission assignment, user sessions, user login history, account security | `/admin/users`, `/admin/users/:id` (tabs) |
| Roles · Permissions · Clients | `/admin/roles` · `/admin/permissions` · `/admin/clients` |
| Login monitoring · Audit logs · System configuration | `/admin/login-history` · `/admin/audit-logs` · `/admin/system` |

Some endpoints do not exist in the current backend — see **docs/API_CONTRACT.md**. Those pages show a "Sample data" banner
and use `src/api/demo.js` until the server implements them.

## Layout
`src/auth` PKCE + token handling · `src/api` http client, endpoints, demo fallback · `src/i18n` en/km dictionaries ·
`src/components` UI kit, shell · `src/pages/{auth,account,admin}`.
