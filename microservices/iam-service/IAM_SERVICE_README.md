# iam-service — Identity & Access Management on Spring Authorization Server

OAuth 2.1 / OpenID Connect 1.0 provider where **User, Role and Permission are the core domain** and Spring
Authorization Server is just an adapter. Includes Dynamic Client Registration, social login (Google, GitHub,
Facebook, Telegram), a JWT‑secured REST API (RFC 9457 errors) and JTE pages in English and Khmer.

## Modules & dependency rules

```
iam-service/
├── iam-domain/
│   ├── iam-domain-core/          plain Java: User, Role, Permission, Identity, assignments, AuthorityCalculator
│   └── iam-application-service/  one class per use case, commands/results, output ports  (→ domain-core)
├── iam-persistence/              JPA entities, Spring Data repos, MapStruct, port adapters   (→ application-service)
├── iam-infrastructure/           Authorization Server, JDBC stores, claims, token customizer, DCR, social login (→ application-service)
├── iam-rest-api/                 controllers, DTOs, validation, JWT API security, Problem Details (→ application-service)
├── iam-web/                      JTE pages, web controllers, i18n                              (→ application-service)
└── iam-main/                     Spring Boot app, wiring, config, Liquibase master, tests       (→ all)
```
* `iam-domain-core` / `iam-application-service` contain **no** JPA, web or Spring Authorization Server code
  (the application layer only uses `@Service`/`@Transactional`).
* Output ports: `UserRepository`, `RoleRepository`, `PermissionRepository` (persistence), `PasswordHasher`,
  `CurrentActorProvider` (infrastructure) and `ClientConsentPort` (infrastructure; lets `iam-web` show consent / connected apps
  without depending on Spring Authorization Server).
* Controllers only map DTO ⇄ command/result and call exactly one use case.

## Run it

```bash
cp .env.example .env && set -a && source .env && set +a
docker compose up -d                      # PostgreSQL
mvn -q -DskipTests install
java -jar iam-main/target/iam-main-1.0.0.jar     # http://localhost:9000
```
Required env: `DB_PASSWORD`, `IAM_DCR_CLIENT_SECRET`. With `IAM_ADMIN_*` set, an administrator (role `ADMIN`) is created at
startup. No secret is stored in the repository; defaults in `application.yml` only exist for non-secret values.
Without `IAM_JWT_PRIVATE_KEY/PUBLIC_KEY` an ephemeral RSA key is generated (tokens stop validating after a restart).

Generate a stable key pair:
```bash
openssl genrsa 2048 | openssl pkcs8 -topk8 -nocrypt -outform DER | base64 -w0   # IAM_JWT_PRIVATE_KEY
openssl rsa -in <(openssl genrsa 2048) -pubout -outform DER | base64 -w0         # must be the matching public key
```

## Design notes

* **Soft delete / audit / locking everywhere**: every aggregate has `is_deleted/deleted_*`, `created_*/updated_*` (Spring Data
  auditing, actor = JWT `sub` or `system`) and `@Version`. Hibernate `@SQLRestriction` hides deleted rows. Unique keys are *partial*
  indexes (`WHERE is_deleted = FALSE`) so a deleted user frees its username/email.
* **Assignments** (`user_roles`, `role_permissions`, `user_permissions`) are their own entities (no `@ManyToMany`), audited and
  **revoked, never deleted**; a partial unique index allows only one active row per pair.
* **Effective permissions** = active role permissions + direct `GRANT` − direct `DENY` (`AuthorityCalculator`, unit tested).
  Authorities are `ROLE_<name>` and `resource:action`.
* **Tokens**: `sub` = `users.uuid`; access token carries `roles` and `permissions`; ID token / `/userinfo` carry claims by scope
  (`openid profile email phone address`). The REST API validates our own JWTs and maps those claims to authorities, then guards
  every endpoint with `@PreAuthorize("hasAuthority('user:read')")` etc.
* **Social login**: Google/GitHub/Facebook through Spring OAuth2 client; Telegram through the Login Widget (HMAC verified).
  All go through `FindOrProvisionFederatedUserUseCase`. E‑mail auto‑linking only happens when the provider verified the e‑mail.
  When a user is already signed in, the same flow calls `LinkIdentityUseCase` instead (profile → linked accounts).
  Register callbacks `http://localhost:9000/login/oauth2/code/{google|github|facebook}`; Telegram requires a public domain
  registered with @BotFather (`/setdomain`).
* **Dynamic Client Registration**: `POST /connect/register` with an initial access token obtained by the seeded
  `iam-dcr-initial` client (`client_credentials`, scope `client.create`). Response contains `registration_access_token`.
* **i18n**: `messages.properties` (en) and `messages_km.properties` (km); `?lang=en|km` (cookie). No text in templates.
* **Seed data** (Liquibase): permissions `user|role|permission : create|read|update|delete|assign`, roles `ADMIN` (all) and `USER`
  (read on user/role/permission — adjust in `002-seed-roles-permissions.sql` if users should not list others).

## Endpoints

See the Postman collection in `postman/` (all of Section D plus OIDC/DCR flow). Lists accept `page`, `size`, `sort=field,asc|desc`.
Errors are `application/problem+json`, e.g. validation → `https://iam.istad.co/problems/validation-error` with an `errors` array.

## Tests

```bash
mvn test                 # domain + use-case unit tests
mvn verify               # + IamApplicationIT (Testcontainers/PostgreSQL, needs Docker)
```
