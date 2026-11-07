# IAM Service Implementation & Architecture Guide

A step-by-step guide to understanding, implementing, and maintaining the **Identity & Access Management (`iam-service`)** in the ISTAD microservices ecosystem.

---

## 📑 Table of Contents
1. [Architecture Overview & Design Philosophy](#1-architecture-overview--design-philosophy)
2. [Module Breakdown & Dependency Rules](#2-module-breakdown--dependency-rules)
3. [Step-by-Step Implementation Guide](#3-step-by-step-implementation-guide)
4. [Comprehensive Java File Catalog (Why Each File Exists)](#4-comprehensive-java-file-catalog-why-each-file-exists)
   - [4.1 `iam-domain-core`](#41-iam-domain-core)
   - [4.2 `iam-application-service`](#42-iam-application-service)
   - [4.3 `iam-service-persistence`](#43-iam-service-persistence)
   - [4.4 `iam-service-infrastructure`](#44-iam-service-infrastructure)
   - [4.5 `iam-service-restapi`](#45-iam-service-restapi)
   - [4.6 `iam-service-web`](#46-iam-service-web)
   - [4.7 `iam-service-main`](#47-iam-service-main)
5. [Key Architectural Mechanisms](#5-key-architectural-mechanisms)
6. [Running, Migrating & Verifying](#6-running-migrating--verifying)

---

## 1. Architecture Overview & Design Philosophy

The `iam-service` is an **OAuth 2.1** and **OpenID Connect 1.0** authentication and authorization provider designed around **Domain-Driven Design (DDD)** and **Hexagonal (Ports & Adapters) Clean Architecture**.

### The Core Principle: Domain First
In typical Spring Boot apps, developers often put `@Entity`, Spring Security, and `@RestController` together in single classes. In `iam-service`, **User, Role, and Permission are the pure business core**.
* **Spring Authorization Server is merely an adapter** sitting in the infrastructure layer.
* **JPA/Hibernate is an adapter** sitting in the persistence layer.
* **Spring MVC / REST is an adapter** sitting in the web/api layer.
* If Spring Authorization Server or Spring Data JPA is replaced in the future, **zero lines of business logic in `iam-domain` need to change**.

```
                           ┌──────────────────────────────────────────────┐
                           │               Web Browsers / SPA             │
                           └──────────────────────┬───────────────────────┘
                                                  │
                 ┌────────────────────────────────┴────────────────────────────────┐
                 │                                                                 │
                 ▼                                                                 ▼
      ┌─────────────────────┐                                           ┌─────────────────────┐
      │   iam-service-web   │ (JTE SSR)                              │ iam-service-restapi │ (JSON REST API)
      │  /login, /consent   │                                           │  /api/v1/users,...  │
      └──────────┬──────────┘                                           └──────────┬──────────┘
                 │                                                                 │
                 │              ┌───────────────────────────────────┐              │
                 └─────────────►│     iam-application-service       │◄─────────────┘
                                │ (Use Cases, Commands, Results)    │
                                └─────────┬───────────────┬─────────┘
                                          │               │
                                          ▼               │
                                ┌───────────────────┐     │
                                │  iam-domain-core  │     │
                                │ (Entities, Logic) │     │
                                └───────────────────┘     │
                 ┌────────────────────────────────────────┴────────────────────────┐
                 │ (Driven Ports / SPIs)                                           │
                 ▼                                                                 ▼
      ┌─────────────────────────────┐                           ┌──────────────────────────────────────┐
      │   iam-service-persistence   │                           │     iam-service-infrastructure       │
      │  (JPA Entities, Repos, DB)  │                           │ (Spring Auth Server, OAuth2, Crypto) │
      └──────────────┬──────────────┘                           └──────────────────┬───────────────────┘
                     │                                                             │
                     ▼                                                             ▼
           [ PostgreSQL RDBMS ]                                        [ OAuth2 Clients & Social APIs ]
```

---

## 2. Module Breakdown & Dependency Rules

The root project is `microservices:iam-service`, comprised of 7 decoupled Gradle subprojects:

| Module | Purpose | Allowed Dependencies | Forbidden Dependencies |
| :--- | :--- | :--- | :--- |
| **`iam-domain-core`** | Pure Java domain entities, exceptions, authority evaluator | Plain Java, Lombok | No Spring, JPA, or Web |
| **`iam-application-service`** | Business use cases, DTOs (Commands/Results), SPI ports | `iam-domain-core`, `spring-tx`, `spring-context` | No JPA, Security, or Web MVC |
| **`iam-service-persistence`** | Database schema, JPA entities, Spring Data Repositories | `iam-application-service`, Spring Data JPA, PostgreSQL | No Web, Security, or Auth Server |
| **`iam-service-infrastructure`** | Spring Authorization Server, JWT signing, OAuth2 Social login | `iam-application-service`, Spring Security, JDBC | No REST API, Web, or JPA |
| **`iam-service-restapi`** | JSON REST Controllers, Swagger, validation, RFC 9457 errors | `iam-application-service`, Spring Web, Spring Security | No JPA entities or DB Repos |
| **`iam-service-web`** | User-facing SSR templates (Login, Consent, Profile) using JTE | `iam-application-service`, Spring Web, JTE | No JPA entities or DB Repos |
| **`iam-service-main`** | Application runner, Liquibase migrations, Admin bootstrap | All submodules, Spring Boot Starter | None |

---

## 3. Step-by-Step Implementation Guide

If you are creating or re-implementing `iam-service` from scratch, follow these chronological steps:

```
Step 1: Multi-Module Gradle Config & libs.versions.toml
   │
Step 2: iam-domain-core (Pure Java Entities & Authority Calculator)
   │
Step 3: iam-application-service (Use Cases, Commands, Results, Output Ports)
   │
Step 4: iam-service-persistence (JPA Entities, Liquibase DDL, Repository Adapters)
   │
Step 5: iam-service-infrastructure (OAuth2 Authorization Server, JWT & Social Login)
   │
Step 6: iam-service-restapi (REST Controllers, API DTOs, Security Filter Chain)
   │
Step 7: iam-service-web (JTE Views, CSRF, Cookie Locale, Web Controllers)
   │
Step 8: iam-service-main (IamApplication, AdminSeeder, application.yml)
   │
Step 9: Database Setup & End-to-End Verification
```

### Step 1: Define Multi-Project Gradle Build
* Create `settings.gradle` declaring the modules (`iam-domain:iam-domain-core`, `iam-domain:iam-application-service`, `iam-service-persistence`, `iam-service-infrastructure`, `iam-service-restapi`, `iam-service-web`, `iam-service-main`).
* In root `build.gradle`, set `tasks.named('bootJar') { enabled = false }` so the root behaves as an aggregator, and add `allprojects.configurations.configureEach { resolutionStrategy.dependencySubstitution { ... } }` to allow transparent dependency resolution.
* Pin the toolchain across all submodules to **Java 21 LTS**.

### Step 2: Implement Domain Core (`iam-domain-core`)
1. Create value objects and enums: `Effect` (`ALLOW`, `DENY`), `IdentityProvider` (`LOCAL`, `GOOGLE`, `GITHUB`, etc.).
2. Create domain models: `User`, `Role`, `Permission`, `Identity`, `Assignment`, `UserRole`, `RolePermission`, `UserPermission`.
3. Create business exceptions: `DomainException`, `NotFoundException`, `ConflictException`, `BusinessRuleException`.
4. Implement the domain service: `AuthorityCalculator` to compute effective user permissions (taking direct grants + role grants into account with explicit `DENY` overriding `ALLOW`).

### Step 3: Implement Application Layer (`iam-domain-service`)
1. Define **Driven Output Ports** (`port.out.*`): `UserRepository`, `RoleRepository`, `PermissionRepository`, `PasswordHasher`, `CurrentActorProvider`, `ClientConsentPort`.
2. Define DTOs:
   - **Commands**: Immutable input requests (e.g., `CreateUserCommand`, `ChangePasswordCommand`).
   - **Queries**: Filter and pagination criteria (e.g., `UserFilterQuery`).
   - **Results**: Immutable outputs returned to controllers (e.g., `UserResult`, `AuthUserResult`).
3. Implement **Single-Responsibility Use Cases**:
   - Each use case is a `@Service` class ending in `UseCase` containing a single public method `execute(...)`.
   - Examples: `CreateUserUseCase`, `AuthenticateUseCase`, `AssignRolesToUserUseCase`.

### Step 4: Implement Database Persistence (`iam-service-persistence`)
1. Write Liquibase changesets:
   - `001-iam-schema.sql`: tables for `users`, `roles`, `permissions`, `user_roles`, `role_permissions`, `user_permissions`, `identities`.
   - `002-seed-roles-permissions.sql`: seed foundational roles (`SUPER_ADMIN`, `ADMIN`, `USER`) and default permissions.
2. Define JPA entities (`@Entity`, `@Table`) mirroring the SQL tables with auditing base fields (`created_at`, `created_by`, `updated_at`, `updated_by`).
3. Define Spring Data JPA interfaces (`UserJpaRepository`, `RoleJpaRepository`, etc.).
4. Use MapStruct mappers (`UserEntityMapper`, etc.) to map between JPA Entities and Domain Models.
5. Create Adapter classes (`UserRepositoryAdapter`, etc.) implementing the domain's Output Ports.

### Step 5: Implement Infrastructure & Security (`iam-service-infrastructure`)
1. Write OAuth2 authorization server tables:
   - `003-oauth2-schema.sql`: `oauth2_registered_client`, `oauth2_authorization`, `oauth2_authorization_consent`.
2. Configure Spring Authorization Server in `AuthorizationServerConfig`:
   - Endpoints matcher, OIDC openid configuration, JWK source (RSA key pair), Token customizer.
3. Implement `IamTokenCustomizer`: injects user roles and permissions into JWT access tokens and scope-specific claims into ID tokens.
4. Implement Dynamic Client Registration (DCR) via `DcrEndpointFilter` to allow authenticated clients to register programmatically.
5. Configure social login support (`FederatedIdentityAuthenticationSuccessHandler`, `SocialUserInfoExtractor`).

### Step 6: Implement REST API (`iam-service-restapi`)
1. Configure `ApiSecurityConfig`: security filter chain for `/api/**` with stateless JWT Bearer token authentication.
2. Implement REST Controllers: `AuthController`, `UserController`, `RoleController`, `PermissionController`, `MeController`.
3. Configure `ApiExceptionHandler` returning standard **RFC 9457 Problem Details**.

### Step 7: Implement Web UI (`iam-service-web`)
1. Setup **JTE** (Java Template Engine) templates for server-side rendering:
   - `login.jte`, `consent.jte`, `register.jte`, `profile-edit.jte`, `change-password.jte`, `connected-apps.jte`.
2. Create Web Controllers: `LoginController`, `ConsentController`, `RegisterController`, `ProfileController`, `HomeController`.
3. Add i18n support with `messages.properties` (English) and `messages_km.properties` (Khmer).

### Step 8: Assemble in Main Application (`iam-service-main`)
1. Create `IamApplication` annotated with `@SpringBootApplication`.
2. Create `AdminSeeder`: executes at startup to seed the initial root administrator using environment variables (`IAM_ADMIN_USERNAME`, `IAM_ADMIN_PASSWORD`).
3. Write `application.yml` and integration tests (`IamApplicationIT`) using Testcontainers.

---

## 4. Comprehensive Java File Catalog (Why Each File Exists)

Below is an exhaustive breakdown of every Java file in the project, explaining **why** it was created and its exact role in the architecture.

---

### 4.1 `iam-domain-core`
Contains pure Java models and rules. Has zero framework coupling.

#### Package: `kh.edu.istad.platform.iam.domain.model`
* **`User.java`**: Represents the User aggregate root in the domain. Contains identity, profile, account flags (`enabled`, `accountLocked`, `emailVerified`), and password state.
* **`Role.java`**: Domain model representing a role (e.g. `ROLE_ADMIN`, `ROLE_CUSTOMER`). Holds a system flag (`systemRole` prevents deletion) and associated permissions.
* **`Permission.java`**: Domain model for fine-grained action authorization (e.g. `users:read`, `orders:create`).
* **`Identity.java`**: Represents a linked authentication identity (local password credential, or federated social accounts such as Google, GitHub, Telegram).
* **`IdentityProvider.java`**: Enum defining supported identity providers (`LOCAL`, `GOOGLE`, `GITHUB`, `FACEBOOK`, `TELEGRAM`).
* **`Effect.java`**: Enum defining authorization effects: `ALLOW` or `DENY`. Allows explicit denial of permissions even if granted by an assigned role.
* **`Assignment.java`**: Value object encapsulating role or permission assignment status, expiration time, and assignment context.
* **`UserRole.java`**: Junction domain object capturing the relationship and assignment metadata between a `User` and a `Role`.
* **`RolePermission.java`**: Junction domain object capturing the permissions associated with a `Role`.
* **`UserPermission.java`**: Junction domain object capturing direct permission overrides assigned directly to a `User`.
* **`Protectable.java`**: Interface marking resources that cannot be deleted or renamed if marked as system-protected.
* **`Auditable.java`**: Interface defining audit timestamp and author accessors (`createdAt`, `createdBy`, `updatedAt`, `updatedBy`).

#### Package: `kh.edu.istad.platform.iam.domain.exception`
* **`DomainException.java`**: Base unchecked runtime exception for all domain-related failures.
* **`NotFoundException.java`**: Thrown when a requested aggregate root (User, Role, Permission) cannot be found by ID, UUID, or username.
* **`ConflictException.java`**: Thrown on unique constraint violations (e.g. username already taken, email already in use).
* **`BusinessRuleException.java`**: Thrown when an operation violates invariant business rules (e.g. unlinking the user's only remaining login method, deleting a system role).

#### Package: `kh.edu.istad.platform.iam.domain.service`
* **`AuthorityCalculator.java`**: The core authorization engine. Evaluates the effective set of granted authorities for a user. It calculates `(Role Permissions + Direct User Permissions)` and applies `DENY` rules with precedence over `ALLOW`.

---

### 4.2 `iam-application-service`
Contains use cases, commands, queries, results, and boundary ports. Implements the *Single Responsibility Principle* where each business action is an individual class.

#### Package: `kh.edu.istad.platform.iam.application.port.out` (Driven Ports / SPI)
* **`UserRepository.java`**: Interface specifying persistence operations for users, user identities, roles, and direct permissions.
* **`RoleRepository.java`**: Interface specifying persistence operations for roles and role-permission associations.
* **`PermissionRepository.java`**: Interface specifying persistence operations for permission metadata.
* **`PasswordHasher.java`**: Port abstracting cryptographic password hashing (`encode`, `matches`) so domain logic doesn't depend on Spring Security's `PasswordEncoder`.
* **`CurrentActorProvider.java`**: Port retrieving the username of the user performing the current request (for auditing).
* **`ClientConsentPort.java`**: Port allowing web controllers to view and revoke OAuth2 authorized clients and consents without importing Spring Authorization Server classes.

#### Package: `kh.edu.istad.platform.iam.application.command` (Input DTOs)
* **`CreateUserCommand.java`**: Data needed to create a new user internally.
* **`RegisterUserCommand.java`**: Data provided during public self-registration.
* **`UpdateUserProfileCommand.java`**: Profile fields updated by the user (name, bio, phone, avatar).
* **`ChangePasswordCommand.java`**: User self-service password update (requires old password verification).
* **`ResetUserPasswordCommand.java`**: Admin-initiated password reset.
* **`CreateRoleCommand.java`**: Name, description, and initial permissions to create a new role.
* **`UpdateRoleCommand.java`**: New name and description to update an existing role.
* **`CreatePermissionCommand.java`**: Permission slug and human-readable description.
* **`UpdatePermissionCommand.java`**: Permission update payload.
* **`AssignRolesToUserCommand.java`**: List of role IDs to bind to a specific user.
* **`AssignPermissionsToRoleCommand.java`**: List of permission IDs to bind to a role.
* **`GrantPermissionsToUserCommand.java`**: Explicit direct permissions granted/denied to a specific user.
* **`FederatedIdentityCommand.java`**: External OAuth2 profile details (subject ID, email, provider) to link to an account.
* **`AddressCommand.java`**: Structured user address payload (street, city, country).

#### Package: `kh.edu.istad.platform.iam.application.query` (Search Queries)
* **`UserFilterQuery.java`**: Query object for searching users by keyword, role, status, and pagination.
* **`RoleFilterQuery.java`**: Query object for searching roles with pagination.
* **`PermissionFilterQuery.java`**: Query object for searching permissions with pagination.

#### Package: `kh.edu.istad.platform.iam.application.result` (Output DTOs)
* **`UserResult.java`**: Detailed user view including profile, roles, and direct permissions.
* **`UserSummary.java`**: Compact user view for paginated tabular lists.
* **`AuthUserResult.java`**: User details returned upon successful login.
* **`UserAuthoritiesResult.java`**: Effective calculated roles and permissions for a user.
* **`RoleResult.java`**: Detailed view of a role including assigned permissions.
* **`RoleSummary.java`**: Compact role representation for dropdowns and lists.
* **`RoleRef.java`**: Minimal identifier and name reference to a role.
* **`PermissionResult.java`**: Detailed permission record.
* **`RoleAssignmentResult.java`**: Status of a role assignment to a user.
* **`PermissionAssignmentResult.java`**: Status of a permission assignment to a role.
* **`PermissionGrantResult.java`**: Status of a direct permission grant/deny on a user.
* **`IdentityResult.java`**: View of a connected login method (Google, GitHub, Local).
* **`ConnectedAppResult.java`**: OAuth2 client authorized by the user with active scopes and authorization time.
* **`ConsentInfoResult.java`**: Metadata displayed on the OAuth2 consent screen (client name, requested scopes).
* **`AddressResult.java`**: Formatted address view.
* **`PageResult.java`**: Generic pagination container (`items`, `page`, `size`, `totalElements`, `totalPages`).

#### Package: `kh.edu.istad.platform.iam.application.support` (Internal Helpers)
* **`NewUser.java`**: Factory helper constructing a new `User` entity with default flags.
* **`UserAssembler.java`**: Assembles composite `UserResult` objects by fetching and combining user entities, roles, and permissions.
* **`RoleAssembler.java`**: Assembles composite `RoleResult` objects with resolved permission details.
* **`UserPatcher.java`**: Applies partial updates from commands onto an existing `User` entity safely.

#### Package: `kh.edu.istad.platform.iam.application.usecase` (Business Use Cases)
* **`AuthenticateUseCase.java`**: Validates credentials (username/password) and returns the authenticated user.
* **`CreateUserUseCase.java`**: Validates unique constraints, hashes password, and persists a new user.
* **`RegisterUserUseCase.java`**: Handles public registration and assigns the default `USER` role.
* **`GetUserByUuidUseCase.java` / `GetUserByIdUseCase.java`**: Fetches user details by UUID or ID.
* **`GetUserByUsernameUseCase.java` / `GetUserByEmailUseCase.java`**: Looks up users by username or email.
* **`LoadUserForAuthenticationUseCase.java`**: Bridge used by Spring Security to load user details and effective authorities.
* **`SearchUsersUseCase.java`**: Executes paginated search queries for users.
* **`UpdateUserProfileUseCase.java`**: Updates mutable profile fields.
* **`ChangePasswordUseCase.java`**: Verifies old password and updates to a new hashed password.
* **`ResetUserPasswordUseCase.java`**: Administrative password reset without requiring the old password.
* **`LockUserUseCase.java` / `UnlockUserUseCase.java`**: Suspends or restores user access.
* **`EnableUserUseCase.java` / `DisableUserUseCase.java`**: Activates or disables user accounts.
* **`VerifyEmailUseCase.java`**: Marks email address as verified.
* **`DeleteUserUseCase.java`**: Soft-deletes or removes a user.
* **`AssignRolesToUserUseCase.java`**: Assigns roles to a user.
* **`RevokeRolesFromUserUseCase.java`**: Removes roles from a user.
* **`GrantPermissionsToUserUseCase.java`**: Directly grants permissions with `ALLOW` effect.
* **`DenyPermissionsToUserUseCase.java`**: Directly denies permissions with `DENY` effect.
* **`RevokePermissionsFromUserUseCase.java`**: Removes direct user permissions.
* **`GetUserAuthoritiesUseCase.java`**: Calls `AuthorityCalculator` to return effective authorities.
* **`CreateRoleUseCase.java`**: Creates a new business role.
* **`GetRoleByIdUseCase.java` / `GetRoleByCodeUseCase.java`**: Retrieves a role by ID or code.
* **`SearchRolesUseCase.java`**: Lists and filters roles.
* **`UpdateRoleUseCase.java`**: Updates role metadata.
* **`DeleteRoleUseCase.java`**: Deletes a role (rejects if system-protected).
* **`AssignPermissionsToRoleUseCase.java`**: Associates permissions with a role.
* **`RevokePermissionsFromRoleUseCase.java`**: Removes permissions from a role.
* **`CreatePermissionUseCase.java`**: Creates a new permission.
* **`GetPermissionByIdUseCase.java` / `GetPermissionBySlugUseCase.java`**: Retrieves permissions.
* **`SearchPermissionsUseCase.java`**: Lists permissions with filtering.
* **`UpdatePermissionUseCase.java`**: Updates permission details.
* **`DeletePermissionUseCase.java`**: Deletes a permission (rejects if system-protected).
* **`LinkFederatedIdentityUseCase.java`**: Connects a social provider (Google, GitHub) to an existing user.
* **`UnlinkIdentityUseCase.java`**: Disconnects a login provider (rejects if it is the only remaining login method).
* **`ListUserIdentitiesUseCase.java`**: Lists all active linked authentication methods.
* **`GetConsentInfoUseCase.java`**: Retrieves client metadata and requested scopes for the consent screen.
* **`ListConnectedAppsUseCase.java`**: Lists all 3rd-party apps authorized by the user.
* **`RevokeConnectedAppUseCase.java`**: Revokes OAuth2 token grants and consents for an application.

---

### 4.3 `iam-service-persistence`
Implements the domain repository ports using Spring Data JPA and PostgreSQL.

#### Package: `kh.edu.istad.platform.iam.persistence.entity` (JPA Tables)
* **`BaseEntity.java`**: `@MappedSuperclass` holding audit columns (`createdAt`, `createdBy`, `updatedAt`, `updatedBy`, `version`).
* **`UserEntity.java`**: JPA entity mapped to the `users` table.
* **`RoleEntity.java`**: JPA entity mapped to the `roles` table.
* **`PermissionEntity.java`**: JPA entity mapped to the `permissions` table.
* **`IdentityEntity.java`**: JPA entity mapped to `identities` table for local and social credentials.
* **`AssignmentEntity.java`**: Base entity for assignment junction tables.
* **`UserRoleEntity.java`**: JPA entity mapped to `user_roles` table.
* **`RolePermissionEntity.java`**: JPA entity mapped to `role_permissions` table.
* **`UserPermissionEntity.java`**: JPA entity mapped to `user_permissions` table.

#### Package: `kh.edu.istad.platform.iam.persistence.repository` (Spring Data Interfaces)
* **`UserJpaRepository.java`**: Spring Data interface for CRUD and custom queries on `users`.
* **`RoleJpaRepository.java`**: Spring Data interface for `roles`.
* **`PermissionJpaRepository.java`**: Spring Data interface for `permissions`.
* **`IdentityJpaRepository.java`**: Queries identities by provider, subject ID, or user ID.
* **`UserRoleJpaRepository.java`**: Queries user-to-role mappings.
* **`RolePermissionJpaRepository.java`**: Queries role-to-permission mappings.
* **`UserPermissionJpaRepository.java`**: Queries direct user-to-permission overrides.

#### Package: `kh.edu.istad.platform.iam.persistence.mapper` (MapStruct)
* **`UserEntityMapper.java`**: Converts between `User` (Domain) and `UserEntity` (JPA).
* **`RoleEntityMapper.java`**: Converts between `Role` (Domain) and `RoleEntity` (JPA).
* **`PermissionEntityMapper.java`**: Converts between `Permission` (Domain) and `PermissionEntity` (JPA).
* **`IdentityEntityMapper.java`**: Converts between `Identity` (Domain) and `IdentityEntity` (JPA).
* **`IgnoreBaseFields.java`**: Custom MapStruct annotation ignoring `id`, `createdAt`, `version` during updates.

#### Package: `kh.edu.istad.platform.iam.persistence.adapter` (Port Implementations)
* **`UserRepositoryAdapter.java`**: Implements domain's `UserRepository` port by coordinating JPA repositories and mappers.
* **`RoleRepositoryAdapter.java`**: Implements domain's `RoleRepository` port.
* **`PermissionRepositoryAdapter.java`**: Implements domain's `PermissionRepository` port.
* **`PersistenceSupport.java`**: Reusable persistence helpers (Spring Data Page to domain `PageResult` conversion).

#### Package: `kh.edu.istad.platform.iam.persistence.config`
* **`PersistenceConfig.java`**: Configures `@EnableJpaAuditing` and defines the `AuditorAware` bean powered by `CurrentActorProvider`.

---

### 4.4 `iam-service-infrastructure`
Handles low-level protocol adapters: OAuth 2.1 Authorization Server, JWT generation, JDBC token storage, Dynamic Client Registration, and social login.

#### Package: `kh.edu.istad.platform.iam.infrastructure.config`
* **`AuthorizationServerConfig.java`**: Core security bean definitions. Configures protocol endpoints (`/oauth2/token`, `/oauth2/authorize`, `/oauth2/jwks`, `/.well-known/openid-configuration`), registers RSA keys, and sets up login redirection.
* **`InfrastructureConfig.java`**: General infrastructure beans (such as `PasswordEncoder` bean using BCrypt).

#### Package: `kh.edu.istad.platform.iam.infrastructure.oidc`
* **`IamTokenCustomizer.java`**: Intercepts token generation. Enriches JWT Access Tokens with user claims (`sub=uuid`, `roles=[...]`, `permissions=[...]`) and ID Tokens with standard OIDC claims.
* **`OidcClaimsMapper.java`**: Maps user attributes into OIDC standard claims (`name`, `email`, `email_verified`, `picture`, `phone_number`, `address`).

#### Package: `kh.edu.istad.platform.iam.infrastructure.dcr`
* **`DcrEndpointFilter.java`**: Filter implementing **RFC 7591 (Dynamic Client Registration)** at `/connect/register`. Allows microservices and SPA apps to register OAuth2 client IDs and secrets programmatically.

#### Package: `kh.edu.istad.platform.iam.infrastructure.jdbc`
* **`JdbcClientRegistrationRepository.java`**: Implements Spring's `RegisteredClientRepository` on PostgreSQL JDBC so clients are stored persistently in database tables rather than in memory.
* **`JdbcOAuth2AuthorizationService.java`**: Implements `OAuth2AuthorizationService` storing active authorizations and tokens in PostgreSQL.
* **`JdbcOAuth2AuthorizationConsentService.java`**: Implements `OAuth2AuthorizationConsentService` storing granted user consents in PostgreSQL.

#### Package: `kh.edu.istad.platform.iam.infrastructure.security`
* **`CustomUserDetails.java`**: Adapter implementing Spring Security's `UserDetails` backed by the domain's `AuthUserResult`.
* **`IamUserDetailsService.java`**: Implements `UserDetailsService` by delegating to `LoadUserForAuthenticationUseCase`.

#### Package: `kh.edu.istad.platform.iam.infrastructure.social`
* **`FederatedIdentityAuthenticationSuccessHandler.java`**: Handles redirect upon successful social login (Google, GitHub, Facebook), linking the external identity to the local user account automatically.
* **`SocialUserInfoExtractor.java`**: Extracts normalized profile information (subject, email, full name, avatar) from disparate social provider user-info payloads.

#### Package: `kh.edu.istad.platform.iam.infrastructure.adapter`
* **`BCryptPasswordHasher.java`**: Implements domain's `PasswordHasher` port using Spring Security's `BCryptPasswordEncoder`.
* **`CurrentActorProviderAdapter.java`**: Implements domain's `CurrentActorProvider` port by extracting the username from Spring Security's `SecurityContextHolder`.
* **`ClientConsentAdapter.java`**: Implements domain's `ClientConsentPort` by reading directly from JDBC consent tables.

---

### 4.5 `iam-service-restapi`
Exposes the management API for administrative operations and SPA/mobile clients.

#### Package: `kh.edu.istad.platform.iam.rest.config`
* **`ApiSecurityConfig.java`**: Configures the `@Order(2)` HTTP security filter chain for `/api/**`. Mandates stateless JWT bearer tokens and sets up permission checks.

#### Package: `kh.edu.istad.platform.iam.rest.controller`
* **`AuthController.java`**: Endpoints for password-based login and token issuance (`/api/v1/auth/login`).
* **`MeController.java`**: Self-service endpoints for the currently authenticated user (`/api/v1/me`, profile updates, password changes, linked accounts).
* **`UserController.java`**: Administrative user CRUD endpoints (`/api/v1/users`).
* **`RoleController.java`**: Role management endpoints (`/api/v1/roles`).
* **`PermissionController.java`**: Permission querying and registration endpoints (`/api/v1/permissions`).

#### Package: `kh.edu.istad.platform.iam.rest.dto`
* **`ApiMapper.java`**: MapStruct mapper transforming REST request bodies into Domain Commands, and Domain Results into REST Responses.
* **`Common.java`**: Shared API DTOs (pagination requests, error envelopes, status messages).
* **`UserDtos.java`**: Request and response records for user management.
* **`RoleDtos.java`**: Request and response records for roles and permission bindings.
* **`PermissionDtos.java`**: Request and response records for permissions.

#### Package: `kh.edu.istad.platform.iam.rest.error`
* **`ApiExceptionHandler.java`**: `@RestControllerAdvice` mapping domain exceptions (`NotFoundException`, `ConflictException`, `BusinessRuleException`) to HTTP status codes (`404`, `409`, `422`, `400`).
* **`Problems.java`**: Helper serializing RFC 9457 `ProblemDetail` responses.

---

### 4.6 `iam-service-web`
Handles interactive browser sessions: login screen, registration, consent approval, and user profile management.

#### Package: `kh.edu.istad.platform.iam.web`
* **`WebConfig.java`**: Configures Spring MVC web beans (JTE view resolver, cookie locale resolver, language change interceptor).
* **`WebContext.java`**: Context object passed to all JTE templates (CSRF tokens, current user, localized messages).
* **`WebAdvice.java`**: `@ControllerAdvice` injecting common attributes (`WebContext`, active theme, language) into all web views.
* **`CurrentUser.java`**: Model representing the logged-in web user session.
* **`Forms.java`**: Static inner classes for HTML form bindings (LoginForm, RegisterForm, ProfileForm, PasswordForm).

#### Package: `kh.edu.istad.platform.iam.web.controller`
* **`LoginController.java`**: Serves the `/login` page with optional error or logout banners.
* **`ConsentController.java`**: Serves the OAuth2 consent screen (`/oauth2/consent`) where users review requested client scopes.
* **`RegisterController.java`**: Serves and processes public user registration (`/register`).
* **`ProfileController.java`**: Serves the user dashboard, profile editing, and password update forms.
* **`HomeController.java`**: Root redirect controller routing users to `/login` or `/profile`.

---

### 4.7 `iam-service-main`
The executable Spring Boot runner module.

#### Package: `kh.edu.istad.platform.iam`
* **`IamApplication.java`**: The main class containing `public static void main(String[] args)`. Its package `kh.edu.istad.platform.iam` ensures that Spring's `@ComponentScan` automatically covers all submodules without manual configuration.

#### Package: `kh.edu.istad.platform.iam.bootstrap`
* **`AdminSeeder.java`**: `ApplicationRunner` executing at startup. Reads `IAM_ADMIN_USERNAME`, `IAM_ADMIN_EMAIL`, and `IAM_ADMIN_PASSWORD` from environment variables and seeds the default system administrator if no admin account exists.

---

## 5. Key Architectural Mechanisms

### 5.1 Authority Calculation Engine
Permissions can be assigned in two ways:
1. **Via Roles**: A user has roles, and each role has assigned permissions.
2. **Directly**: A user can be granted or denied specific permissions directly.

`AuthorityCalculator` processes this as follows:
```
1. Collect all permissions from active Roles (Effect = ALLOW)
2. Merge direct User Permissions
3. If any direct User Permission has Effect = DENY for a permission slug:
      -> The permission is stripped from the granted set (DENY overrides ALLOW)
4. Resulting set is stamped into the JWT Access Token as "permissions": [...]
```

### 5.2 Dynamic Client Registration (DCR)
Clients (such as new microservices or frontend applications) don't need manual database entries to integrate with OAuth2. By making an authenticated POST request to `/connect/register`, `DcrEndpointFilter` creates a client record in the `oauth2_registered_client` table with client ID, secret, and allowed grant types (`authorization_code`, `refresh_token`, `client_credentials`).

---

## 6. Running, Migrating & Verifying

### Environment Configuration (`.env`)
Create a `.env` file in `microservices/iam-service`:
```bash
DB_URL=jdbc:postgresql://localhost:5432/iam
DB_USERNAME=iam
DB_PASSWORD=secret
IAM_ISSUER=http://localhost:9000
IAM_DCR_CLIENT_SECRET=dcr-secret-key-12345
IAM_ADMIN_USERNAME=admin
IAM_ADMIN_EMAIL=admin@istad.edu.kh
IAM_ADMIN_PASSWORD=AdminPassword123!
```

### Launching PostgreSQL with Docker
```bash
cd microservices/iam-service
docker compose up -d
```

### Building & Running with Gradle
```bash
# Compile all modules
./gradlew compileJava

# Run domain unit tests
./gradlew :iam-domain:iam-domain-core:test :iam-domain:iam-application-service:test

# Build the executable Boot JAR
./gradlew build -x test

# Run the IAM Service (Port 9000)
java -jar iam-service-main/build/libs/iam-service-main-1.0.0.jar
```

### Testing with Postman
Import `postman/iam-service.postman_collection.json` into Postman to test:
* Password grant authentication & token refresh
* Dynamic client registration
* User, role, and permission administration
* RFC 9457 error handling
