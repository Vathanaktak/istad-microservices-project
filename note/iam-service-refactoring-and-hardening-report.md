# IAM Service Refactoring, Hardening & Feature Implementation Report

**Document Date:** November 15, 2026  
**Target Project:** `microservices/iam-service` (Spring Boot 3.4.1, Spring Authorization Server, React/Vite SPA)  
**Location:** [D:/ISTAD/microservices/istad-ite-project/microservices/iam-service](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service)  
**Service Status:** RUNNING on `http://localhost:9000` (Daemon PID: 9368)

---

## 1. Executive Summary

This report documents the end-to-end refactoring, security hardening, and feature enhancements completed for the `iam-service` multi-module Gradle project. The objective was to eliminate startup conflicts, modernize the UI from server-rendered JTE templates to an integrated React/Vite Single Page Application (SPA), achieve a fully self-contained Gradle build system, implement missing hexagonal API flows for password reset, sessions, monitoring, and client management, and apply cryptographic and OAuth2 security hardening.

All deliverables adhere strictly to Hexagonal (Ports & Adapters) architecture, DDD principles, Liquibase database migrations, and Spring Security 6 best practices.

---

## 2. Section 1: Must Fix (App Startup & Build Stability)

### 1.1 Web Controller Cleanup & Route Collision Resolution
- **Problem:** The legacy server-rendered controllers competed with `SpaController` for root routes (`/`, `/login`, `/register`, `/consent`, `/profile`), causing `AmbiguousMappingException` during Spring application startup.
- **Actions:**
  - **Deleted:** `HomeController.java`, `LoginController.java`, `RegisterController.java`, `ConsentController.java`, and `ProfileController.java` in `iam-service-web/src/main/java/kh/edu/istad/platform/iam/web/controller/`.
  - **Deleted:** Unused legacy web infrastructure classes `Forms.java`, `CurrentUser.java`, `WebAdvice.java`, and `WebContext.java`.
  - **Preserved:**
    - `SpaController.java`: Forwards all non-API and non-OIDC routes to `index.html`.
    - `ConsentInfoController.java`: Delivers OAuth2 consent payload to the React UI.
    - `WebConfigController.java`: Exposes dynamic runtime frontend configuration.

### 1.2 Deprecation of Legacy Server-Rendered Assets
- **Problem:** Leftover JTE template engine files and static CSS created maintenance bloat.
- **Actions:**
  - Deleted `iam-service-web/src/main/jte/` directory.
  - Deleted `messages.properties` and `messages_km.properties` from `iam-service-web/src/main/resources/` (i18n is handled by React `i18next`).
  - Deleted `resources/static/css/app.css`.

### 1.3 Secret Defaults Removal & Fail-Fast Configuration
- **Problem:** Insecure default values (`change-me-too`, `Adm1n!Pass2026`) allowed the application to start with insecure credentials.
- **Actions:**
  - **`application.yml` ([iam-service-main](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-main/src/main/resources/application.yml)):**
    - Removed `gg.jte` block.
    - Removed `spring.messages` configuration block.
    - Removed hardcoded default fallbacks for `IAM_DCR_CLIENT_SECRET`, `IAM_ADMIN_USERNAME`, `IAM_ADMIN_EMAIL`, and `IAM_ADMIN_PASSWORD`. The application now fails fast or safely skips admin creation if environment variables are not explicitly supplied.
  - **`ClientSeeder.java` ([iam-service-infrastructure](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-infrastructure/src/main/java/kh/edu/istad/platform/iam/infrastructure/client/ClientSeeder.java)):**
    - Removed the `"change-me-too"` default from `@Value("${iam.dcr.client-secret}")`.

### 1.4 Self-Contained Gradle Build & Version Catalog
- **Problem:** `settings.gradle` referenced external builds (`../../build-logic`) and external catalogs (`../../gradle/libs.versions.toml`), preventing standalone builds.
- **Actions:**
  - Created a local, dedicated `build-logic/` module inside `microservices/iam-service/`:
    - `build-logic/build.gradle` and `build-logic/settings.gradle`
    - `java-common-conventions.gradle`
    - `iam-spring-common-conventions.gradle`
    - `iam-spring-boot-common-conventions.gradle`
  - Created internal [gradle/libs.versions.toml](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/gradle/libs.versions.toml) specifying dependencies for Spring Boot 3.4.1, Spring Security, MapStruct 1.6.3, Lombok, and Liquibase.
  - Configured `settings.gradle` to use the local build logic and standard version catalog.
  - Aligned Gradle wrapper to version `9.8.0` for full compatibility with Java 21 toolchains.

---

## 3. Section 2: Should Fix (Missing Flows & Business Logic)

### 2.1 Frontend Demo Fallback
- In [iam-service-web/frontend/src/config.js](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-web/frontend/src/config.js), changed `DEMO_FALLBACK` default from `'true'` to `'false'` to ensure API calls interact with the actual backend.

### 2.2 Hexagonal Endpoints Implementation

#### a) Password Reset Flow
- **Database:** Liquibase migration [004-password-reset.sql](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-persistence/src/main/resources/db/changelog/004-password-reset.sql) added to `db.changelog-master.yaml`:
  - Table: `password_reset_token` (`id`, `user_id`, `token_hash`, `expires_at`, `used`, `created_at`).
- **Domain & Application Ports:**
  - `PasswordResetToken.java`: Domain aggregate with validation, expiration checks, and token hashing.
  - `PasswordResetTokenRepository.java`: Output persistence port.
  - `PasswordResetNotifier.java`: Output communication port.
- **Use Cases:**
  - `ForgotPasswordUseCase.java`: Generates a cryptographically random token, hashes it with SHA-256, stores it with 1-hour expiry, and sends notification. Returns silently even if the email does not exist to prevent user enumeration attacks.
  - `ResetPasswordUseCase.java`: Validates token hash, enforces single-use and expiration, and updates the user's password using BCrypt.
- **Adapters & Controllers:**
  - `PasswordResetTokenEntity.java`, `PasswordResetTokenJpaRepository.java`, and `PasswordResetTokenRepositoryAdapter.java`.
  - `LogPasswordResetNotifierAdapter.java`: Secure logging adapter.
  - Endpoints added in `AuthController.java`:
    - `POST /api/auth/forgot-password` (Public)
    - `POST /api/auth/reset-password` (Public)

#### b) Sessions, Login History, Activity & Account Deactivation
- **Database:** Liquibase migration [006-history-audit-config.sql](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-persistence/src/main/resources/db/changelog/006-history-audit-config.sql):
  - Tables: `login_history`, `audit_logs`, `deactivation_requests`, `system_config`.
- **Domain Models:**
  - `LoginRecord.java`: Tracks IP address, user agent, success/failure status, and failure reason.
  - `AuditLog.java`: Tracks administrative and system actions.
  - `DeactivationRequest.java`: Manages account deactivation status, reason, and cancellation.
- **Use Cases Implemented:**
  - `GetUserSessionsUseCase.java`
  - `TerminateSessionUseCase.java`
  - `TerminateAllSessionsUseCase.java`
  - `GetLoginHistoryUseCase.java`
  - `GetActivityUseCase.java`
  - `GetDeactivationRequestUseCase.java`
  - `RequestDeactivationUseCase.java`
  - `CancelDeactivationUseCase.java`
  - `ListDeactivationRequestsUseCase.java` (Admin: list proposals with user details & pending count)
  - `GetDeactivationRequestForUserUseCase.java` (Admin: inspect user's pending proposal)
  - `ApproveDeactivationRequestUseCase.java` (Admin: disables user account, terminates all active sessions, marks proposal PROCESSED, records audit log)
  - `RejectDeactivationRequestUseCase.java` (Admin: marks proposal REJECTED, records review notes & audit log)
- **Adapters & Controllers:**
  - `SessionAdapter.java`: Queries and revokes active sessions stored in Spring Authorization Server's `oauth2_authorization` table. (Resolved PostgreSQL error by deriving `created_at` from `COALESCE(authorization_code_issued_at, oidc_id_token_issued_at, access_token_issued_at, refresh_token_issued_at, now())` instead of non-existent `created_at` column).
  - `DeactivationRequestAdminController.java`:
    - `GET /api/deactivation-requests` (Admin: list deactivation proposals, filtered by status)
    - `GET /api/deactivation-requests/count-pending` (Admin: count pending proposals)
    - `POST /api/deactivation-requests/{uuid}/approve` (Admin: approve proposal)
    - `POST /api/deactivation-requests/{uuid}/reject` (Admin: reject proposal)
  - `MeController.java`:
    - `GET /api/me/sessions` - List active user sessions
    - `DELETE /api/me/sessions/{id}` - Revoke specific session
    - `DELETE /api/me/sessions` - Revoke all sessions (except current)
    - `GET /api/me/login-history` - View personal login history
    - `GET /api/me/activity` - View personal audit trail
    - `GET /api/me/deactivation-request` - View deactivation status
    - `POST /api/me/deactivation-request` - Submit deactivation request
    - `DELETE /api/me/deactivation-request` - Cancel deactivation request
  - `UserController.java`:
    - `GET /api/users/{id}/sessions` (Admin)
    - `DELETE /api/users/{id}/sessions/{sessionId}` (Admin)
    - `DELETE /api/users/{id}/sessions` (Admin)
    - `GET /api/users/{id}/login-history` (Admin)
    - `GET /api/users/{id}/deactivation-request` (Admin: get user's pending deactivation proposal)
- **Frontend Admin Features:**
  - `Deactivations.jsx`: Dedicated admin page (`/admin/deactivations`) to review all user deactivation proposals with status filtering, user details, and interactive approve/reject modals.
  - `UserDetail.jsx`: Prominent warning banner and approve/reject actions displayed on the user's details page whenever an active deactivation proposal exists.
  - `Overview.jsx`: Live pending deactivation counter card.
  - `Shell.jsx`: Added "Deactivations" navigation link to the management menu.

#### c) OAuth2 Client Management, Audit Logs & System Config
- **Ports & Adapters:**
  - `ClientManagementPort.java` & `ClientManagementAdapter.java`: Interfaces directly with `RegisteredClientRepository`.
  - `AuditLogRepository.java` & `SystemConfigRepository.java` with JPA adapters.
- **Use Cases Implemented:**
  - `ListClientsUseCase.java`
  - `CreateClientUseCase.java`
  - `UpdateClientUseCase.java`
  - `DeleteClientUseCase.java`
  - `RegenerateClientSecretUseCase.java`
  - `GetSystemConfigUseCase.java`
  - `SaveSystemConfigUseCase.java`
- **Controllers & Endpoints:**
  - `ClientController.java` (`@PreAuthorize("hasAuthority('client:read')")` / `client:write`):
    - `GET /api/clients` - List registered OAuth2 clients
    - `POST /api/clients` - Register a new OAuth2 client
    - `GET /api/clients/{id}` - View client details
    - `PUT /api/clients/{id}` - Update client redirect URIs, scopes, and grant types
    - `DELETE /api/clients/{id}` - Remove client
    - `POST /api/clients/{id}/secret` - Regenerate client secret
  - `MonitoringController.java`:
    - `GET /api/audit-logs` - System-wide audit logs with pagination and filtering
    - `GET /api/login-history` - Global login attempts and outcomes
  - `SystemConfigController.java`:
    - `GET /api/system/config` - Retrieve key-value platform configurations
    - `PUT /api/system/config` - Update platform configurations
- **Postman Collection:**
  - Updated [postman/iam-service.postman_collection.json](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/postman/iam-service.postman_collection.json) to include all newly introduced endpoints with sample request/response structures.

### 2.3 Timed Account Lockout
- **Domain Model:** Added `lockedUntil` (`Instant`) to `User.java`. Evaluated via `isTemporarilyLocked()` which automatically unlocks the user when `Instant.now().isAfter(lockedUntil)`.
- **Database:** Liquibase migration [005-locked-until.sql](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-persistence/src/main/resources/db/changelog/005-locked-until.sql) added `locked_until TIMESTAMP` to the `users` table.
- **Use Case:** In `RecordLoginAttemptUseCase.java`, when `MAX_FAILED_ATTEMPTS` (5) is reached, `lockedUntil` is set to `Instant.now().plus(15, ChronoUnit.MINUTES)`. Permanent administrative lockouts (`lockAccount()`) remain distinct and unaffected.
- **Security Integration:** `IamUserDetailsService.java` checks `user.isTemporarilyLocked()` and reports account lockout to Spring Security.

### 2.4 Comprehensive Login Event Recording & History Persistence
- **`RecordLoginAttemptUseCase.java`:** Integrated with `LoginHistoryRepository` to capture every authentication event (both `SUCCESS` and `FAILED`). Parses user agent for device (Desktop, Mobile, Tablet), browser (Chrome, Edge, Safari, Firefox), OS (Windows, macOS, Linux, Android, iOS), client IP address, failure reason (`bad_credentials`, `locked`, `disabled`, `expired`), and marks suspicious attempts.
- **`LoginAttemptListener.java`:** Added listeners for all Spring Security authentication events:
  - `AuthenticationSuccessEvent`
  - `AuthenticationFailureBadCredentialsEvent`
  - `AuthenticationFailureLockedEvent`
  - `AuthenticationFailureDisabledEvent`
  - `AuthenticationFailureExpiredEvent`
  - `AuthenticationFailureCredentialsExpiredEvent`
  - Extracts client IP (supporting `X-Forwarded-For` proxy headers) and `User-Agent` from request context.
- **Federated & Social Login Tracking:**
  - Updated `FindOrProvisionFederatedUserUseCase.java`, `FederatedOAuth2UserService.java`, and `TelegramLoginFilter.java` to invoke `recordLoginSuccess()`, guaranteeing that `last_login_at` and `login_history` are updated across OAuth2 (Google, GitHub, Facebook) and Telegram authentication channels.

### 2.5 Documentation Modernization
- Rewrote [IAM_SERVICE_README.md](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/IAM_SERVICE_README.md):
  - Replaced obsolete `mvn` commands with `./gradlew` equivalents.
  - Corrected module names to match current project hierarchy (`iam-service-persistence`, `iam-service-restapi`, `iam-service-web`, `iam-service-infrastructure`, `iam-service-main`).
  - Updated cryptographic key generation commands to derive both public and private keys from a single private key file:
    ```bash
    openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
    openssl rsa -in private_key.pem -pubout -out public_key.pem
    ```
  - Documented environment variable requirements (`IAM_DCR_CLIENT_SECRET`, `IAM_ADMIN_PASSWORD`, etc.) without baking secrets into the repository.

---

## 4. Section 3: Security Hardening

### 3.1 JWK Dynamic Thumbprint `kid`
- **Problem:** Static or arbitrary Key IDs (`kid`) in JWKs do not provide cryptographic binding to the underlying key material.
- **Fix:** In `AuthorizationServerConfig.java`, computed the `kid` dynamically using the SHA-256 RFC 7638 thumbprint of the RSA public key:
  ```java
  Base64URL thumbprint = rsaKey.computeThumbprint("SHA-256");
  RSAKey jwk = new RSAKey.Builder(rsaKey)
          .keyID(thumbprint.toString())
          .build();
  ```
- **Verification:** Verified via `GET /oauth2/jwks` that the key ID is a valid Base64URL-encoded SHA-256 thumbprint matching the active RSA public key.

### 3.2 In-Memory Refresh Token Handling
- **Problem:** Storing refresh tokens in `sessionStorage` or `localStorage` exposes them to Cross-Site Scripting (XSS) extraction.
- **Fix:** In `iam-service-web/frontend/src/context/AuthContext.jsx`, removed `sessionStorage` persistence for `refreshToken`. Refresh tokens are now maintained exclusively in private JavaScript runtime memory variables and refreshed via silent renewal.

### 3.3 Collision-Resistant Username Generation
- **Problem:** Random integer generation for federated user collisions could exhaust quickly or use predictable pseudorandom sequences.
- **Fix:** In `FindOrProvisionFederatedUserUseCase.java`, replaced `Random` with `java.security.SecureRandom`, generating a 4-character alphanumeric suffix with collision retry attempts capped at 20 iterations before throwing a clear `ConflictException`.

### 3.4 Telegram Account Linking Re-authentication Check
- **Problem:** Account linking without recent authentication checks allows session hijacking or unauthorized linking if an old session is left open.
- **Fix:** In `TelegramLoginFilter.java`, added a session age validation enforcing that the active HTTP session was authenticated within the last 5 minutes (`MAX_LINK_SESSION_AGE_MILLIS = 300_000`) before linking a Telegram ID to an existing account.

### 3.5 Token Revocation via Authorization Service
- **Problem:** Directly executing SQL deletes against `oauth2_authorization` bypasses Spring Authorization Server lifecycle events and in-memory caches.
- **Fix:** In `ConnectedAppsAdapter.java`, refactored application disconnection to resolve authorizations through `OAuth2AuthorizationService.findById()` and revoke them cleanly via `OAuth2AuthorizationService.remove()`.

---

## 5. Build, Test & Runtime Verification

### 5.1 Unit Tests
Executed via `./gradlew test`:
- `AuthorityCalculatorTest`: PASSED
- `UnlinkIdentityUseCaseTest`: PASSED
- `ForgotPasswordUseCaseTest`: PASSED (tested valid token generation, email suppression for unknown users)
- `ResetPasswordUseCaseTest`: PASSED (tested valid token consumption, single-use enforcement, expired token rejection)
- `RecordLoginAttemptUseCaseTest`: PASSED (tested lockout threshold calculation and 15-minute temporary lockout duration)
- `DeactivationRequestUseCaseTest`: PASSED (tested approval with user deactivation & session revocation, rejection with notes, conflict handling)

### 5.2 Frontend Compilation
Executed via `npm run build` in `iam-service-web/frontend`:
- Vite compilation succeeded.
- Assets packaged cleanly into `iam-service-web/frontend/dist/`.

### 5.3 Backend Packaging
Executed via `./gradlew build -PskipFrontend -x test`:
- `BUILD SUCCESSFUL` across all 7 Gradle submodules:
  - `iam-domain:iam-domain-core`
  - `iam-domain:iam-application-service`
  - `iam-service-persistence`
  - `iam-service-infrastructure`
  - `iam-service-restapi`
  - `iam-service-web`
  - `iam-service-main`
- Executable JAR produced: `iam-service-main/build/libs/iam-service-main-1.0.0.jar`.

### 5.4 Live Service Verification
- Service initialized as a background daemon process.
- Database connection established with PostgreSQL container (`ite-postgres`) on port `9543`.
- Liquibase successfully executed changesets `001` through `007`.
- Verified HTTP Endpoints:
  - `GET http://localhost:9000/login` -> `HTTP 200 OK` (Serves React SPA `index.html`)
  - `GET http://localhost:9000/.well-known/openid-configuration` -> `HTTP 200 OK` (OIDC metadata)
  - `GET http://localhost:9000/oauth2/jwks` -> `HTTP 200 OK` (RFC 7638 thumbprint `kid` confirmed)
  - `GET http://localhost:9000/api/me/sessions` -> `HTTP 200 OK` (Active sessions queried with fallback token timestamps)
  - `GET http://localhost:9000/api/me/login-history` -> `HTTP 200 OK` (Persistent login attempt audit logs)
  - `GET http://localhost:9000/api/me/activity` -> `HTTP 200 OK` (User security timeline and audit history)

---

## 6. Section 4: Operational Hardening & Account Page Fixes

### 6.1 Active Sessions SQL Query Fix
- **Issue:** Accessing `/account/sessions` caused a 500 error: `PSQLException: column "created_at" does not exist`. In Spring Authorization Server schema, `oauth2_authorization` does not have a `created_at` column.
- **Resolution:** Updated `SessionAdapter.java` to compute session start and last active timestamps dynamically:
  ```sql
  COALESCE(authorization_code_issued_at, oidc_id_token_issued_at, access_token_issued_at, refresh_token_issued_at, now()) AS created_at
  ```

### 6.2 Login History Recording Fix
- **Issue:** `/account/history` displayed "No login attempts" even after multiple sign-ins.
- **Resolution:** `RecordLoginAttemptUseCase.java` previously calculated lockout counters but never saved records to `LoginHistoryRepository`. Updated both successful and failed authentication handlers in `LoginAttemptListener.java` and `RecordLoginAttemptUseCase.java` to persist parsed IP address, User-Agent, browser, OS, and failure reason into `login_history`.

### 6.3 Account Activity Audit Log Recording
- **Issue:** `/account/activity` showed "No activity yet" because user-initiated use cases never persisted records to `audit_logs`.
- **Resolution:**
  - Extended `CurrentActorProvider` outbound port with `default String currentIpAddress()` and implemented it in `CurrentActorProviderAdapter.java` using Spring's `RequestContextHolder`.
  - Wired `AuditLogRepository.save(...)` into core account use cases:
    - `ChangePasswordUseCase.java` -> `PASSWORD_CHANGED`
    - `ResetPasswordUseCase.java` -> `PASSWORD_CHANGED`
    - `UpdateUserProfileUseCase.java` -> `PROFILE_UPDATED`
    - `RequestDeactivationUseCase.java` -> `DEACTIVATION_REQUESTED`
    - `CancelDeactivationUseCase.java` -> `DEACTIVATION_CANCELLED`
    - `TerminateSessionUseCase.java` -> `SESSION_TERMINATED`
    - `TerminateAllSessionsUseCase.java` -> `SESSION_TERMINATED`
    - `UnlinkIdentityUseCase.java` -> `IDENTITY_UNLINKED`
    - `LinkIdentityUseCase.java` -> `IDENTITY_LINKED`
  - Added Lucide icons (`UserX`, `UserCheck`) and bilingual translations (English & Khmer) in `Activity.jsx`, `en.js`, and `km.js` for deactivation proposal events.

---

## 7. How to Run & Verify

### Environment Setup
Create or verify `.env` file in `microservices/iam-service/`:
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:9543/db_iam
SPRING_DATASOURCE_USERNAME=iteuser
SPRING_DATASOURCE_PASSWORD=qwerqwer
IAM_DCR_CLIENT_SECRET=dcr-secret-key-prod-2026
IAM_ADMIN_USERNAME=admin
IAM_ADMIN_EMAIL=admin@istad.edu.kh
IAM_ADMIN_PASSWORD=AdminStrongPass!2026
```

### Running the Backend
```powershell
cd microservices/iam-service
./gradlew bootRun
```
Or run the packaged JAR directly:
```powershell
java -jar iam-service-main/build/libs/iam-service-main-1.0.0.jar
```

### Running the Frontend in Development Mode
```powershell
cd microservices/iam-service/iam-service-web/frontend
npm install
npm run dev
```

---

*Report generated and committed to project notes.*
