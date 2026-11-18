# 🧠 IAM Service — Master Architecture & System Reference

> **Purpose of this document:**  
> This file is the single source of truth for the `iam-service` project. Whenever an AI assistant is asked to work on `iam-service`, reading this file provides the complete context, architecture, module relationships, database schema, security flows, API routes, and operational instructions without needing to inspect individual files.

---

## 1. Project Overview & Technology Stack

- **Project Name:** `iam-service` (Identity & Access Management)
- **Role in Platform:** Centralized Identity Provider (IdP) & Single Sign-On (SSO) service for the ISTAD microservices ecosystem.
- **Java / Framework:** Java 21, Spring Boot `3.4.1`, Spring Authorization Server `1.4.1`, Spring Security 6
- **Architecture Style:** Hexagonal Architecture (Ports and Adapters / Clean Architecture) + Domain-Driven Design (DDD)
- **Frontend:** React 19 Single Page Application (Vite, Tailwind CSS, Lucide icons, custom i18n English/Khmer) embedded into the backend JAR under `/static`
- **Database:** PostgreSQL on port `9543` (Database name: `db_iam` or `iam`)
- **Schema Management:** Liquibase migrations (`001` through `007`)
- **Build Tool:** Gradle multi-module build with dedicated local `build-logic`
- **Runtime Port:** HTTP `9000` (`http://localhost:9000`)

---

## 2. Multi-Module Project Structure

The project is divided into clean hexagonal layers:

```
microservices/iam-service/
├── build-logic/                           # Internal Gradle plugins & build scripts
├── iam-domain/
│   ├── iam-domain-core/                   # Pure Domain Models, Value Objects, Domain Exceptions
│   └── iam-application-service/           # Use Cases, Commands, Queries, Outbound Port Interfaces
├── iam-service-infrastructure/            # Spring Security configs, OAuth2 SAS, External Adapters
├── iam-service-persistence/               # Liquibase changelogs, JPA Entities, Repositories, DB Adapters
├── iam-service-restapi/                   # REST Controllers, DTOs, Problem Details exception advice
├── iam-service-web/                       # SpaController, Consent/WebConfig APIs, React SPA (frontend/)
└── iam-service-main/                      # Application entry point, application.yml, Seeders
```

### Module Responsibilities:

1. **`iam-domain-core`**:
   - Zero framework dependencies (no Spring annotations).
   - Core Entities: `User`, `Role`, `Permission`, `UserRole`, `RolePermission`, `UserPermission`, `Identity`, `DeactivationRequest`, `LoginRecord`, `AuditLog`, `PasswordResetToken`, `SystemConfig`.
   - Value Objects: `Address`, `Name`, `Email`, `Username`.
   - Domain Service: `IamDomainService.java` (handles business validations, state transitions, password hashing assignments).
   - Domain Exceptions: `NotFoundException`, `ConflictException`, `BusinessRuleException`, `UnauthorizedException`, `ValidationException`.

2. **`iam-application-service`**:
   - Inbound Use Cases: 
     - **Auth & Account:** `RegisterUserUseCase`, `ChangePasswordUseCase`, `ResetPasswordUseCase`, `ForgotPasswordUseCase`, `UpdateUserProfileUseCase`, `LinkIdentityUseCase`, `UnlinkIdentityUseCase`, `RequestDeactivationUseCase`, `CancelDeactivationUseCase`, `GetActivityUseCase`, `GetLoginHistoryUseCase`.
     - **Sessions:** `GetUserSessionsUseCase`, `TerminateSessionUseCase`, `TerminateAllSessionsUseCase`.
     - **Admin Management:** `ListUsersUseCase`, `GetUserUseCase`, `CreateUserUseCase`, `DeleteUserUseCase`, `EnableUserUseCase`, `DisableUserUseCase`, `LockUserUseCase`, `UnlockUserUseCase`, `ListDeactivationRequestsUseCase`, `ApproveDeactivationRequestUseCase`, `RejectDeactivationRequestUseCase`.
     - **RBAC & Clients:** `ListRolesUseCase`, `CreateRoleUseCase`, `UpdateRoleUseCase`, `DeleteRoleUseCase`, `ListPermissionsUseCase`, `ListClientsUseCase`, `CreateClientUseCase`, `UpdateClientUseCase`, `DeleteClientUseCase`, `RegenerateClientSecretUseCase`.
     - **Monitoring & Audit:** `GetAuditLogsUseCase`, `RecordLoginAttemptUseCase`, `GetSystemConfigUseCase`, `SaveSystemConfigUseCase`.
   - Outbound Ports (Interfaces):
     - `UserRepository`, `RoleRepository`, `PermissionRepository`, `AuditLogRepository`, `LoginHistoryRepository`, `DeactivationRequestRepository`, `PasswordResetTokenRepository`, `SystemConfigRepository`, `SessionPort`, `CurrentActorProvider`, `PasswordHasher`, `PasswordResetNotifier`.

3. **`iam-service-infrastructure`**:
   - `AuthorizationServerConfig.java`: Spring Authorization Server configuration, OIDC endpoints, JWK source with RFC 7638 SHA-256 thumbprint `kid`.
   - `DefaultSecurityConfig.java`: Form login, session fixation protection, social federation filter chain.
   - `SessionAdapter.java`: Implements `SessionPort`, queries Spring Authorization Server's `oauth2_authorization` using token issue timestamp fallbacks (`COALESCE(authorization_code_issued_at, oidc_id_token_issued_at, access_token_issued_at, refresh_token_issued_at, now())`).
   - `LoginAttemptListener.java`: Listens to Spring Security authentication success and failure events, extracts client IP (handling `X-Forwarded-For`), and triggers `RecordLoginAttemptUseCase`.
   - `CurrentActorProviderAdapter.java`: Implements `CurrentActorProvider` with `currentActor()` and `currentIpAddress()`.

4. **`iam-service-persistence`**:
   - Implements outbound repository ports using Spring Data JPA and entity mappers.
   - Liquibase master: `db.changelog-master.yaml`.

5. **`iam-service-restapi`**:
   - Controllers: `AuthController`, `MeController`, `UserController`, `ClientController`, `DeactivationRequestAdminController`, `MonitoringController`, `SystemConfigController`.
   - Global Error Handling: `GlobalExceptionHandler.java` produces RFC 7807 Problem Details (`application/problem+json`).

6. **`iam-service-web`**:
   - `SpaController.java`: Routes frontend HTML5 browser paths (`/login`, `/register`, `/account/*`, `/admin/*`) to `index.html`.
   - `ConsentInfoController.java`: Provides client scope and branding payload for `/oauth2/consent`.
   - `WebConfigController.java`: Serves runtime frontend settings (`client_id`, `scopes`, etc.).
   - `frontend/`: Full React 19 application.

---

## 3. Database Schema & Liquibase Migrations

All migrations reside in `iam-service-persistence/src/main/resources/db/changelog/`:

1. **`001-init.sql`**:
   - `users`: ID, UUID, username, email, password_hash, phone_number, first_name, last_name, nickname, gender, birthdate, address fields, enabled, account_non_locked, created_at, updated_at.
   - `roles` & `permissions`: RBAC core definitions.
   - `users_roles`, `roles_permissions`, `users_permissions`: Junction tables.
   - `identities`: Federated OAuth2 identities (Google, GitHub, Telegram).
   - `system_config`: Key-value application configurations.
2. **`002-oauth2.sql`**:
   - `oauth2_registered_client`: Client registration data.
   - `oauth2_authorization`: Active token and authorization records.
   - `oauth2_authorization_consent`: Scopes consented by users.
3. **`003-seed.sql`**:
   - Seed data for default roles, permissions, administrative scopes, and default OAuth2 clients.
4. **`004-password-reset.sql`**:
   - `password_reset_tokens`: User ID, token hash, expiration timestamp, used flag.
5. **`005-locked-until.sql`**:
   - Added `locked_until` (TIMESTAMP) column to `users` for temporary brute-force lockout.
6. **`006-history-audit-config.sql`**:
   - `login_history`: ID, user_id, username, status (`SUCCESS`/`FAILED`), ip_address, location, user_agent, device, browser, os, suspicious, failure_reason, occurred_at.
   - `audit_logs`: ID, uuid, user_id, category, action, outcome, actor, target, ip_address, detail, occurred_at.
7. **`007-deactivation-review.sql`**:
   - `deactivation_requests`: ID, uuid, user_id, reason, details, status (`PENDING`, `PROCESSED`, `CANCELLED`, `REJECTED`), requested_at, processed_at, cancelled_at.
   - Added `review_notes` (TEXT) and `reviewed_by` (VARCHAR).

---

## 4. Frontend Architecture & Token Storage

### Routes (`App.jsx`):
- **Public / Auth:** `/login`, `/register`, `/forgot-password`, `/reset-password`, `/oauth2/consent`, `/auth/callback`.
- **User Portal:** `/account/profile`, `/account/security`, `/account/linked`, `/account/sessions`, `/account/login-history`, `/account/activity`, `/account/deactivation`.
- **Admin Portal (Protected by RBAC Authorities):**
  - `/admin` (Overview)
  - `/admin/users` & `/admin/users/:id` (User Management & Deactivation proposal banner)
  - `/admin/deactivations` (Dedicated deactivation proposal review queue)
  - `/admin/roles` & `/admin/permissions` (RBAC)
  - `/admin/clients` (OAuth2 Registered Clients)
  - `/admin/login-history` (Global Login Monitor)
  - `/admin/audit-logs` (Global System Audit Log)
  - `/admin/system` (Dynamic System Configurations)

### Token Storage Strategy (`AuthContext.jsx`):
- **OAuth 2.1 Authorization Code Flow with PKCE (`S256`)**:
  - `access_token`: Stored in `sessionStorage` (`iam.tokens`). Cleared on browser tab close.
  - `refresh_token`: **Kept in JavaScript memory only** (`memRefreshToken`). Never saved to `localStorage` or `sessionStorage` to mitigate persistent token theft via XSS.
  - `JSESSIONID` Cookie: Managed by Spring Authorization Server with `HttpOnly` and `SameSite=Lax`. When the page reloads and memory is wiped, silent re-authentication executes seamlessly using this cookie.
  - Automatic Silent Refresh: `getToken()` in `AuthContext.jsx` automatically refreshes the token if it expires in less than 30 seconds.

---

## 5. Key Workflows & Business Rules

1. **Account Deactivation Proposal Workflow**:
   - Users submit a deactivation request with a reason and details at `/account/deactivation`.
   - The status is marked `PENDING`. The user can still log in and can cancel the request at any time.
   - Administrators see the request in two places:
     - The dedicated queue at `/admin/deactivations`.
     - An alert banner at `/admin/users/:id`.
   - **Approval (`ApproveDeactivationRequestUseCase`):**
     - Disables user (`enabled = false`).
     - Forcefully terminates all active sessions across all devices (`sessionPort.terminateAllSessions(username)`).
     - Marks request status as `PROCESSED`, sets `processed_at`, `reviewed_by`, and `review_notes`.
     - Logs an immutable audit entry in `audit_logs`.
   - **Rejection (`RejectDeactivationRequestUseCase`):**
     - Marks request status as `REJECTED`, sets `review_notes` and `reviewed_by`. User remains enabled.

2. **Social Account Unlink Protection**:
   - In `UnlinkIdentityUseCase`, if a user has no local password hash and only 1 linked social account, unlinking is rejected (`Cannot unlink the last sign-in method`) to prevent permanently locking the user out.

3. **Brute-Force & Audit Trail**:
   - Failed authentication attempts trigger `RecordLoginAttemptUseCase` to store IP, browser, OS, device, and failure reason in `login_history`.
   - User security operations (`PASSWORD_CHANGED`, `PROFILE_UPDATED`, `DEACTIVATION_REQUESTED`, `SESSION_TERMINATED`, `IDENTITY_LINKED`, `IDENTITY_UNLINKED`) automatically save records into `audit_logs` with client IP via `CurrentActorProvider`.

---

## 6. How to Build, Test, and Run

### Environment Variables (`microservices/iam-service/.env`):
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:9543/db_iam
SPRING_DATASOURCE_USERNAME=iteuser
SPRING_DATASOURCE_PASSWORD=qwerqwer
IAM_DCR_CLIENT_SECRET=dcr-secret-key-prod-2026
IAM_ADMIN_USERNAME=admin
IAM_ADMIN_EMAIL=admin@istad.edu.kh
IAM_ADMIN_PASSWORD=AdminStrongPass!2026
```

### 1. Build Frontend:
```powershell
Set-Location "D:\ISTAD\microservices\istad-ite-project\microservices\iam-service\iam-service-web\frontend"
npm run build
```
*(On Windows sandbox, run with elevated permissions if Vite temp directory throws transient `EPERM`).*

### 2. Build Backend JAR:
```powershell
Set-Location "D:\ISTAD\microservices\istad-ite-project\microservices\iam-service"
./gradlew build -PskipFrontend -x test
```
*(Always execute Gradle commands with `BypassSandbox: true` so the Gradle daemon can access caches in `C:\Users\Admin\.gradle`).*

### 3. Run Backend Service (Port 9000):
```powershell
$envFile = "D:\ISTAD\microservices\istad-ite-project\microservices\iam-service\.env";
Get-Content $envFile | Where-Object { $_ -match '^([^#=]+)=(.*)$' } | ForEach-Object {
    $name = $matches[1].Trim()
    $val = $matches[2].Trim()
    [System.Environment]::SetEnvironmentVariable($name, $val, 'Process')
};
Set-Location "D:\ISTAD\microservices\istad-ite-project\microservices\iam-service";
java -jar iam-service-main/build/libs/iam-service-main-1.0.0.jar
```
*(Run outside sandbox with `BypassSandbox: true` and `IsDaemon: true` so browser can connect on `http://localhost:9000`).*

---

## 7. Related Project Documentation

- **Full Refactoring & Hardening Report:** [iam-service-refactoring-and-hardening-report.md](file:///D:/ISTAD/microservices/istad-ite-project/note/iam-service-refactoring-and-hardening-report.md)
- **English Page-by-Page Demo Script:** [iam-project-demo-script-en.md](file:///D:/ISTAD/microservices/istad-ite-project/note/iam-project-demo-script-en.md)
- **Khmer Page-by-Page Demo Script:** [iam-project-demo-script-kh.md](file:///D:/ISTAD/microservices/istad-ite-project/note/iam-project-demo-script-kh.md)
