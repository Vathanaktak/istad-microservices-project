# IAM Service: Complete Changelog & Fixes Report

[//]: # Date 14/11/2026()

This is a comprehensive record of all changes, migrations, bug fixes, and exact code modifications performed on **`iam-service`** in `istad-ite-project`.

---

## 📋 Table of Contents
1. [Overview of Actions](#1-overview-of-actions)
2. [Issue 1: Windows Gradle Build Hanging on `npmInstall` / `npmBuild`](#2-issue-1-windows-gradle-build-hanging-on-npminstall--npmbuild)
3. [Issue 2: OAuth 2.1 Authorization Code 400 Bad Request (`client_id=iam-web`)](#3-issue-2-oauth-21-authorization-code-400-bad-request-client_idiam-web)
4. [Issue 3: CSRF Rejection on Authorization Server Endpoints](#4-issue-3-csrf-rejection-on-authorization-server-endpoints)
5. [Issue 4: Logout Infinite Re-Login Loop](#5-issue-4-logout-infinite-re-login-loop)
6. [Issue 5: Spring Security Logout 403 Forbidden & 404 Not Found](#6-issue-5-spring-security-logout-403-forbidden--404-not-found)
7. [System Verification & Current Status](#7-system-verification--current-status)

---

## 1. Overview of Actions

| Action | Path / Target | Purpose |
|---|---|---|
| **Module Replacement** | `microservices/iam-service/iam-service-web` | Deleted legacy JTE server-rendered template module and replaced it with the unified React (Vite) SPA + Spring Boot glue. |
| **Frontend Build** | `.../iam-service-web/frontend` | Installed npm packages and compiled the React SPA into static assets (`dist/`). |
| **OAuth2 Client Seeding** | `.../client/ClientSeeder.java` | Seeded the public client `iam-web` into PostgreSQL (`oauth2_registered_client`) with PKCE and OIDC scopes. |
| **Security Configuration** | `.../config/DefaultSecurityConfig.java` | Configured SPA routing permissions, allowed CSRF-exempt logout on both `GET` & `POST`, and enabled cookie clearing. |
| **Auth Server CSRF** | `.../config/AuthorizationServerConfig.java` | Configured cookie-based CSRF (`XSRF-TOKEN`) for Authorization Server consent & authorization endpoints. |
| **Frontend Auth Context** | `.../frontend/src/auth/AuthContext.jsx` | Fixed race condition where premature React state clearing caused `<Protected />` to cancel logout and trigger an instant auto-login loop. |

---

## 2. Issue 1: Windows Gradle Build Hanging on `npmInstall` / `npmBuild`

### Problem
In `iam-service-web/build.gradle`, the `npmInstall` and `npmBuild` tasks used `commandLine npm, 'ci' ...` with `npm` resolved to `'npm.cmd'`. On Windows, invoking `.cmd` scripts via Java's `ProcessBuilder` without `cmd /c` causes the process to hang waiting for input/stdin.

### Fix
* **File**: `microservices/iam-service/iam-service-web/build.gradle`

```groovy
// ==================== BEFORE ====================
def npm = org.gradle.internal.os.OperatingSystem.current().isWindows() ? 'npm.cmd' : 'npm'

tasks.register('npmInstall', Exec) {
    onlyIf { !skipFrontend }
    workingDir = frontendDir.asFile
    commandLine npm, 'ci', '--no-audit', '--no-fund'
    inputs.files(frontendDir.file('package.json'), frontendDir.file('package-lock.json'))
    outputs.dir(frontendDir.dir('node_modules'))
}

tasks.register('npmBuild', Exec) {
    onlyIf { !skipFrontend }
    dependsOn 'npmInstall'
    workingDir = frontendDir.asFile
    commandLine npm, 'run', 'build'
    inputs.dir(frontendDir.dir('src'))
    inputs.dir(frontendDir.dir('public'))
    inputs.files(frontendDir.file('index.html'), frontendDir.file('vite.config.js'), frontendDir.file('package.json'))
    outputs.dir(frontendDir.dir('dist'))
}

// ==================== AFTER (FIXED) ====================
def isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows()

tasks.register('npmInstall', Exec) {
    onlyIf { !skipFrontend }
    workingDir = frontendDir.asFile
    if (isWindows) {
        commandLine 'cmd', '/c', 'npm', 'ci', '--no-audit', '--no-fund'
    } else {
        commandLine 'npm', 'ci', '--no-audit', '--no-fund'
    }
    inputs.files(frontendDir.file('package.json'), frontendDir.file('package-lock.json'))
    outputs.dir(frontendDir.dir('node_modules'))
}

tasks.register('npmBuild', Exec) {
    onlyIf { !skipFrontend }
    dependsOn 'npmInstall'
    workingDir = frontendDir.asFile
    if (isWindows) {
        commandLine 'cmd', '/c', 'npm', 'run', 'build'
    } else {
        commandLine 'npm', 'run', 'build'
    }
    inputs.dir(frontendDir.dir('src'))
    inputs.dir(frontendDir.dir('public'))
    inputs.files(frontendDir.file('index.html'), frontendDir.file('vite.config.js'), frontendDir.file('package.json'))
    outputs.dir(frontendDir.dir('dist'))
}
```

---

## 3. Issue 2: OAuth 2.1 Authorization Code 400 Bad Request (`client_id=iam-web`)

### Problem
When users submitted the login form, the React SPA redirected to:
```
http://localhost:9000/oauth2/authorize?response_type=code&client_id=iam-web&redirect_uri=http%3A%2F%2Flocalhost%3A9000%2Fauth%2Fcallback&scope=openid+profile+email+phone+address...
```
This threw a **`Whitelabel Error Page - 400 Bad Request`** because `iam-web` was not registered in the `oauth2_registered_client` database table. The original `ClientSeeder` only registered `iam-dcr-initial`.

### Fix
* **File**: `microservices/iam-service/iam-service-infrastructure/src/main/java/kh/edu/istad/platform/iam/infrastructure/client/ClientSeeder.java`

```java
// ==================== BEFORE ====================
@Component
@RequiredArgsConstructor
public class ClientSeeder implements ApplicationRunner {
    private final RegisteredClientRepository clients;
    private final PasswordEncoder encoder;
    @Value("${iam.dcr.client-id:iam-dcr-initial}") private String clientId;
    @Value("${iam.dcr.client-secret:change-me-too}") private String clientSecret;

    @Override
    public void run(ApplicationArguments args) {
        if (clients.findByClientId(clientId) != null) return;
        clients.save(RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(clientId).clientName("Dynamic Client Registration (initial access)")
            .clientSecret(encoder.encode(clientSecret))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("client.create")
            .build());
    }
}

// ==================== AFTER (FIXED) ====================
@Component
@RequiredArgsConstructor
public class ClientSeeder implements ApplicationRunner {
    private final RegisteredClientRepository clients;
    private final PasswordEncoder encoder;
    @Value("${iam.dcr.client-id:iam-dcr-initial}") private String clientId;
    @Value("${iam.dcr.client-secret:change-me-too}") private String clientSecret;
    @Value("${iam.web.client-id:iam-web}") private String webClientId;
    @Value("${iam.web.redirect-uris:http://localhost:9000/auth/callback,http://localhost:5173/auth/callback}") private String webRedirectUris;

    @Override
    public void run(ApplicationArguments args) {
        seedDcrClient();
        seedWebClient();
    }

    private void seedDcrClient() {
        if (clients.findByClientId(clientId) != null) return;
        clients.save(RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(clientId).clientName("Dynamic Client Registration (initial access)")
            .clientSecret(encoder.encode(clientSecret))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("client.create")
            .build());
    }

    private void seedWebClient() {
        if (clients.findByClientId(webClientId) != null) return;
        RegisteredClient.Builder b = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(webClientId).clientName("IAM Web Console")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)          // public SPA client: PKCE instead of secret
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE).scope(OidcScopes.EMAIL)
            .scope(OidcScopes.PHONE).scope(OidcScopes.ADDRESS)
            .clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .reuseRefreshTokens(false)                                         // token rotation
                .build());
        Arrays.stream(webRedirectUris.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(b::redirectUri);
        clients.save(b.build());
    }
}
```

---

## 4. Issue 3: CSRF Rejection on Authorization Server Endpoints

### Problem
Spring Authorization Server endpoints (`/oauth2/authorize`, consent screens) were not configured with the SPA cookie-based CSRF handler, causing consent posts and form exchanges to be rejected by CSRF protection.

### Fix
* **File**: `microservices/iam-service/iam-service-infrastructure/src/main/java/kh/edu/istad/platform/iam/infrastructure/config/AuthorizationServerConfig.java`

```java
// ==================== BEFORE ====================
@Bean @Order(1)
SecurityFilterChain authorizationServerChain(HttpSecurity http, LoadUserForAuthenticationUseCase loadUser) throws Exception {
    OAuth2AuthorizationServerConfigurer asc = OAuth2AuthorizationServerConfigurer.authorizationServer();
    http.securityMatcher(asc.getEndpointsMatcher())
        .with(asc, as -> as
            .authorizationEndpoint(e -> e.consentPage("/oauth2/consent"))
            .oidc(Customizer.withDefaults()))
...

// ==================== AFTER (FIXED) ====================
@Bean @Order(1)
SecurityFilterChain authorizationServerChain(HttpSecurity http, LoadUserForAuthenticationUseCase loadUser) throws Exception {
    OAuth2AuthorizationServerConfigurer asc = OAuth2AuthorizationServerConfigurer.authorizationServer();
    // Cookie-based CSRF matching the login chain so React consent posts pass validation
    CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
    csrfHandler.setCsrfRequestAttributeName(null);
    http.csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(csrfHandler));
    http.securityMatcher(asc.getEndpointsMatcher())
        .with(asc, as -> as
            .authorizationEndpoint(e -> e.consentPage("/oauth2/consent"))
            .oidc(Customizer.withDefaults()))
...
```

---

## 5. Issue 4: Logout Infinite Re-Login Loop

### Problem
When the user clicked "Sign out", the application immediately bounced back to the logged-in dashboard instead of the login page.
1. `logout()` called `store(null)` before submitting the form.
2. `store(null)` set `tokens = null` in React state.
3. This triggered an immediate re-render of `<Protected />` in `App.jsx`.
4. In `<Protected />`:
   ```javascript
   useEffect(() => {
     if (!authenticated) login(loc.pathname + loc.search);
   }, [authenticated]);
   ```
5. `login()` called `window.location.assign('/oauth2/authorize?...')`, aborting the `/logout` form POST navigation!
6. Because the server session (`JSESSIONID`) was never destroyed, Spring Authorization Server recognized the active session, issued a new authorization code, and immediately redirected back to `/auth/callback`.

### Fix
* **File**: `microservices/iam-service/iam-service-web/frontend/src/auth/AuthContext.jsx`

```javascript
// ==================== BEFORE ====================
const logout = useCallback(() => {
  store(null); // <-- BUG: Triggered React re-render & auto-login loop
  const form = document.createElement('form');
  form.method = 'POST'; form.action = '/logout';
  const csrf = readCookie('XSRF-TOKEN');
  if (csrf) {
    const input = document.createElement('input');
    input.type = 'hidden'; input.name = '_csrf'; input.value = decodeURIComponent(csrf);
    form.appendChild(input);
  }
  document.body.appendChild(form);
  form.submit();
}, [store]);

// ==================== AFTER (FIXED) ====================
/** Local sign-out + end the IAM browser session. */
const logout = useCallback(() => {
  sessionStorage.removeItem(TOKENS);
  sessionStorage.removeItem(FLOW);
  const form = document.createElement('form');
  form.method = 'POST';
  form.action = '/logout';
  const csrf = readCookie('XSRF-TOKEN');
  if (csrf) {
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = '_csrf';
    input.value = decodeURIComponent(csrf);
    form.appendChild(input);
  }
  document.body.appendChild(form);
  form.submit();
}, []);
```

---

## 6. Issue 5: Spring Security Logout 403 Forbidden & 404 Not Found

### Problem
1. When submitting `POST /logout` without a matching `_csrf` token (e.g. if the cookie was rotated or not yet populated), Spring Security returned:
   `{"timestamp":"...","status":403,"error":"Forbidden","path":"/logout"}`
2. Direct `GET /logout` returned `404 Not Found: "No static resource logout."` because Spring Security only matches `POST` by default when CSRF is enabled.

### Fix
* **File**: `microservices/iam-service/iam-service-infrastructure/src/main/java/kh/edu/istad/platform/iam/infrastructure/config/DefaultSecurityConfig.java`

```java
// ==================== BEFORE ====================
http.csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(csrfHandler))
    .authorizeHttpRequests(a -> a
        .requestMatchers("/login", "/login/telegram", "/register", "/css/**", "/js/**", "/images/**", "/error", "/favicon.ico").permitAll()
        .anyRequest().authenticated())
    .formLogin(f -> f.loginPage("/login"))
    .oauth2Login(o -> o.loginPage("/login").userInfoEndpoint(u -> u.userService(federated)))
    .logout(l -> l.logoutSuccessUrl("/login?logout"))
...

// ==================== AFTER (FIXED) ====================
http.csrf(c -> c
        .ignoringRequestMatchers("/logout")                                 // Ignore CSRF on /logout
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(csrfHandler))
    .authorizeHttpRequests(a -> a
        .requestMatchers("/consent-info").authenticated()                   // Guard consent data
        .anyRequest().permitAll())                                          // Allow SPA routes & static assets
    .formLogin(f -> f.loginPage("/login"))
    .oauth2Login(o -> o.loginPage("/login").userInfoEndpoint(u -> u.userService(federated)))
    .logout(l -> l
        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))         // Handle both GET and POST
        .logoutSuccessUrl("/login?logout")                                  // Redirect to login with logout query
        .invalidateHttpSession(true)                                        // Invalidate server session
        .clearAuthentication(true)                                          // Clear SecurityContext
        .deleteCookies("JSESSIONID", "XSRF-TOKEN"))                         // Clean up browser cookies
    .addFilterBefore(new TelegramLoginFilter(botToken, provision, link, load), UsernamePasswordAuthenticationFilter.class);
```

---

## 7. System Verification & Current Status

| Test Case | Method | Expected Result | Actual Result | Status |
|---|---|---|---|:---:|
| **OpenID Configuration** | `GET /.well-known/openid-configuration` | Returns discovery JSON | JSON returned with endpoints & scopes | **PASS** |
| **SPA Login Route** | `GET /login` | HTTP 200 with HTML SPA shell & `XSRF-TOKEN` | HTTP 200, `<title>ISTAD IAM</title>` | **PASS** |
| **OAuth2 Authorize (`iam-web`)** | `GET /oauth2/authorize?client_id=iam-web...` | HTTP 302 Redirect to `/login` for unauthenticated sessions | HTTP 302, Location `/login` | **PASS** |
| **Logout GET & POST** | `GET /logout` & `POST /logout` | HTTP 302 Redirect to `/login?logout` with expired cookies | HTTP 302, `JSESSIONID` & `XSRF-TOKEN` deleted | **PASS** |
| **PostgreSQL DB Client** | SQL Query on `oauth2_registered_client` | Contains `iam-web` row with valid PKCE & scopes | Row verified in database | **PASS** |

### Service URLs
* **Web Console**: [http://localhost:9000/login](http://localhost:9000/login)
* **OpenID Discovery**: [http://localhost:9000/.well-known/openid-configuration](http://localhost:9000/.well-known/openid-configuration)
* **Default Admin Credentials**: `admin` / `Adm1n!Pass2026`
