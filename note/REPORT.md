# Engineering Work Report & Gateway Security Implementation

**Project:** ISTAD Microservices Architecture (`istad-ite-project`)  
**Branch:** `istad-vanda`  
**Date:** Wednesday, November 11, 2026  
**Status:** Completed & Successfully Deployed to Remote  

---

## Part 1: Summary of Work Completed (November 11, 2026)

### 1. IAM Service (`iam-service`) Startup & OAuth 2.0 Configuration
- **Issue:** The service failed to boot due to an `IllegalStateException` resulting from empty OAuth 2.0 client configurations for Google.
- **Root Cause:** Incomplete client provider entries and missing property interpolations in `application.yaml`.
- **Resolution:**
  - Configured OAuth 2.0 client registrations for GitHub and Google using environment variable references (`${GITHUB_CLIENT_ID}`, `${GOOGLE_CLIENT_ID}`) with `.env` fallbacks.
  - Leveraged Spring Security's native `CommonOAuth2Provider` defaults for standard discovery.
  - Successfully executed database migrations via Liquibase against PostgreSQL `db_oauth2` on port `9543`.
  - Service confirmed running and healthy on port `9000` (`http://localhost:9000/.well-known/openid-configuration`).

---

### 2. Postman Collection for OAuth 2.0 / OIDC Testing
- **Deliverable:** Created a production-grade Postman Collection at [`postman/iam-service.postman_collection.json`](../postman/iam-service.postman_collection.json).
- **Features Included:**
  1. **Authorization Code Exchange (Basic Auth):** Standard OAuth 2.0 code exchange via `POST /oauth2/token`.
  2. **Authorization Code Exchange (PKCE / Public Client):** Supports single-page/mobile flows with `code_verifier`.
  3. **Client Credentials Grant:** Machine-to-machine authentication.
  4. **Refresh Token Grant:** Automatic token rotation and access token renewal.
  5. **Discovery & JWKS Endpoints:** `/.well-known/openid-configuration` and `/oauth2/jwks`.
  6. **UserInfo Endpoint:** Authenticated profile retrieval (`/userinfo`).
  7. **Automation Scripts:** Pre-configured test scripts that parse response payloads and automatically persist `access_token`, `refresh_token`, and `id_token` into collection-level variables.

---

### 3. Gateway Server Consul Health Check Fix
- **Issue:** `gateway-server` was flagged as `critical` / `unhealthy` in HashiCorp Consul (`CheckID: service:gateway-server-9095`).
- **Root Causes:**
  1. **YAML Indentation Bug:** `consul:` was indented under `spring.security:` instead of `spring.cloud:`. Spring Cloud Consul defaulted the health check URL to `http://MSI-F13MG:9095/actuator/health`. Inside the Consul Docker container, the host Windows hostname (`MSI-F13MG`) was unresolvable (`dial tcp: lookup MSI-F13MG: no such host`).
  2. **Missing `@Bean` & Unauthenticated Actuator:** `SecurityConfig.java` lacked the `@Bean` annotation on `gatewayFilterChain` and protected all endpoints without permitting `/actuator/**`, returning `401 Unauthorized` to health probes.
- **Resolution:**
  - Repositioned `consul:` under `spring.cloud:` in `microservices/gateway-server/src/main/resources/application.yaml`, setting `health-check-url: http://host.docker.internal:${server.port}/actuator/health`.
  - Added `@Bean` and explicitly configured `exchange.pathMatchers("/actuator/**").permitAll()` in `SecurityConfig.java`.
  - Deregistered stale critical instances via Consul REST API and restarted `gateway-server`.
  - **Verification:** Consul health status transitioned to **`passing`** (`HTTP GET ... 200 OK: {"status":"UP"}`).

---

### 4. Login Page Modernization & Buffer Commit Bug Fix
- **UI Modernization:**
  - Redesigned [`login.html`](../microservices/iam-service/src/main/resources/templates/login.html) with a responsive, modern dual-pane layout.
  - Left hero section features ISTAD IAM branding, dark indigo gradients, and security feature highlights.
  - Right section includes input field icons, password show/hide toggle, dynamic alerts (`?error`, `?logout`), and branded Google & GitHub buttons.
- **Bug Fix (Missing Username & Password Inputs):**
  - **Problem:** When loading `/login`, the username and password boxes disappeared.
  - **Root Cause:** Due to the expanded template size (>8KB), Tomcat flushed response headers before Thymeleaf reached the form. Evaluating `${_csrf.parameterName}` triggered deferred token resolution via `request.getSession()`, causing:
    ```
    java.lang.IllegalStateException: Cannot create a session after the response has been committed
    ```
    Thymeleaf aborted rendering immediately before the input fields.
  - **Resolution:** In `IamServiceApplication.java`, injected `CsrfToken csrfToken` into `@GetMapping("/login")` and called `csrfToken.getToken()`. This forces token generation and session initialization *prior* to view rendering, ensuring clean 100% page delivery.

---

### 5. GitHub Push Protection & Branch Deployment
- **Issue:** `git push origin istad-vanda` was rejected with `GH013: Repository rule violations (Push cannot contain secrets)`.
- **Root Cause:** Hardcoded default values containing Google OAuth Client ID and secret were staged in `microservices/iam-service/src/main/resources/application.yaml`.
- **Resolution:**
  - Stripped all hardcoded credentials from YAML files and bound them strictly to environment variables (`.env` is properly ignored in `.gitignore`).
  - Amended the commit cleanly: `git commit -a --amend --no-edit`.
  - Pushed to remote:
    ```
    383b96c..a6f9fe5  istad-vanda -> istad-vanda
    ```

---

## Part 2: How to Implement Security in Spring Cloud Gateway

Spring Cloud Gateway is built on **Spring WebFlux (Project Reactor)**, which is non-blocking and reactive. Implementing security requires using **Spring Security Reactive (`ServerHttpSecurity`)**, not the traditional servlet-based `HttpSecurity`.

```
                +-----------------------------------------------+
                |            Client (Browser / App)             |
                +-----------------------------------------------+
                                        |
                                        v
        +---------------------------------------------------------------+
        |                   Spring Cloud Gateway (9095)                 |
        |  +---------------------------------------------------------+  |
        |  | SecurityWebFilterChain:                                 |  |
        |  |  1. Permit /actuator/health for Consul                   |  |
        |  |  2. Validate JWT via OAuth2 Resource Server              |  |
        |  |  3. Handle Login via OAuth2 Client (BFF)                |  |
        |  +---------------------------------------------------------+  |
        |                               | TokenRelay                    |
        +---------------------------------------------------------------+
                 |                      |                       |
                 v                      v                       v
        +------------------+  +-------------------+  +--------------------+
        |  order-service   |  | customer-service  |  |  business-service  |
        |  (Resource Svr)  |  |  (Resource Svr)   |  |   (Resource Svr)   |
        +------------------+  +-------------------+  +--------------------+
```

---

### Step 1: Add Dependencies in `build.gradle`

In `microservices/gateway-server/build.gradle`:

```groovy
dependencies {
    // Reactive Gateway & Actuator
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway-server-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // Reactive Security & OAuth2
    implementation 'org.springframework.boot:spring-boot-starter-security-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-security-oauth2-resource-server'
    
    // Consul Discovery
    implementation libs.spring.cloud.starter.consul.discovery
}
```

---

### Step 2: Configure `application.yaml`

In `microservices/gateway-server/src/main/resources/application.yaml`:

```yaml
server:
  port: 9095

spring:
  application:
    name: gateway-server

  cloud:
    # 1. Consul Discovery Configuration
    consul:
      discovery:
        health-check-url: http://host.docker.internal:${server.port}/actuator/health
        service-name: gateway-server
        instance-id: ${spring.application.name}-${random.value}
        prefer-ip-address: true

    # 2. Gateway Routes & Token Relay
    gateway:
      server:
        webflux:
          discovery:
            locator:
              enabled: true
              lower-case-service-id: true
          routes:
            - id: order-service-route
              uri: lb://order-service
              predicates:
                - Path=/api/orders/**
              filters:
                # Passes incoming Bearer token downstream to order-service
                - TokenRelay=
                - RewritePath=/api/orders/?(?<segment>.*), /$\{segment}

  # 3. Security: Resource Server & OAuth2 Client
  security:
    oauth2:
      # Validates incoming JWT Bearer tokens issued by IAM
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000

      # Enables Gateway to act as OAuth2 Client (BFF pattern)
      client:
        provider:
          iam-service:
            issuer-uri: http://localhost:9000
        registration:
          front-bff-client:
            provider: iam-service
            client-id: front-bff-client
            client-authentication-method: none
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:9095/login/oauth2/code/front-bff-client
            scope:
              - openid
              - profile
```

---

### Step 3: Implement Reactive `SecurityConfig.java`

Create `SecurityConfig.java` in `kh.edu.istad.platform.gateway.config`:

```java
package kh.edu.istad.platform.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain gatewayFilterChain(ServerHttpSecurity http) {
        return http
                // 1. URL Authorization Rules
                .authorizeExchange(exchange -> exchange
                        // Health checks & monitoring MUST be public for Consul
                        .pathMatchers("/actuator/**").permitAll()
                        // Public routes (if any)
                        .pathMatchers("/public/**", "/jph/**").permitAll()
                        // All other incoming traffic requires valid authentication
                        .anyExchange().authenticated()
                )

                // 2. Disable unused traditional web auth
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 3. Enable OAuth2 Login for Browser Sessions (BFF pattern)
                .oauth2Login(Customizer.withDefaults())

                // 4. Enable JWT Validation for API Clients (Mobile / External APIs)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }
}
```

---

### Step 4: Downstream Token Propagation (`TokenRelay`)

1. **Client sends request**: `Authorization: Bearer <JWT>` to `http://localhost:9095/api/orders/...`
2. **Gateway validates**: `gateway-server` validates the JWT token against `http://localhost:9000/oauth2/jwks`.
3. **Gateway forwards**: The `TokenRelay=` route filter copies the token and forwards it downstream to `order-service`.
4. **Microservice secures**: Downstream microservices configure a resource server to validate the same token:
   ```yaml
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: http://localhost:9000
   ```

---

### Key Security Checklist:

| Item | Requirement | Common Pitfall |
| :--- | :--- | :--- |
| **Framework** | Use `@EnableWebFluxSecurity` & `ServerHttpSecurity` | Do not use `@EnableWebSecurity` (blocking servlet). |
| **Bean Definition** | Method returning `SecurityWebFilterChain` must have `@Bean` | Missing `@Bean` causes Spring to fall back to default blocking filter. |
| **Actuator Probes** | Must permit `/actuator/health` or `/actuator/**` | Missing `permitAll()` causes Consul status to become `critical` (401). |
| **Consul Indentation** | Place `consul:` under `spring.cloud:` | Indenting under `spring.security` disables custom health URLs. |
| **Token Relay** | Add `- TokenRelay=` filter to route definitions | Without it, downstream microservices receive requests without credentials. |
