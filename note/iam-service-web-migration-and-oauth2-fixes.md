# IAM Service Web Console Migration, OAuth 2.1 & Logout Fixes Report

This document records the migration of the IAM web module in `istad-ite-project/microservices/iam-service`, the diagnosis and resolution of the OAuth 2.1 Authorization Code (`iam-web`) 400 Bad Request error, and the fix for the logout redirect loop.

---

## 1. Summary of Actions Taken

1. **Replaced Web Module**:
   - Removed the obsolete server-rendered JTE web module at `istad-ite-project/microservices/iam-service/iam-service-web`.
   - Copied the unified React (Vite) + Spring SPA module from `iam-service/iam-service-web`.
2. **Fixed Windows Gradle Build**:
   - Fixed `build.gradle` in `iam-service-web` so `npm ci` and `npm run build` invoke via `cmd /c` on Windows.
3. **Registered Public OAuth 2.1 Client (`iam-web`)**:
   - Fixed `ClientSeeder.java` to automatically register the public client `iam-web` required by the React frontend.
4. **Resolved OAuth 2.1 Authorization 400 Bad Request**:
   - Spring Authorization Server now recognizes `iam-web`, enabling PKCE authentication and authorization code issuance.
5. **Configured Authorization Server CSRF**:
   - Added cookie-based CSRF support to `AuthorizationServerConfig.java` to support the SPA consent and authorization endpoints.
6. **Fixed Logout Redirect Loop & CSRF Blocking**:
   - Fixed `AuthContext.jsx` to prevent premature re-render that triggered an automatic re-login loop.
   - Configured `DefaultSecurityConfig.java` to support both `GET` and `POST` for `/logout`, ignore CSRF checks on `/logout`, clear session cookies, and redirect to `/login?logout`.

---

## 2. Detailed Code Changes & Fixes

### A. Windows Gradle Execution Fix
* **File**: `microservices/iam-service/iam-service-web/build.gradle`
* **Issue**: Direct invocation of `npm.cmd` via Gradle `Exec` without `cmd /c` caused build processes to block indefinitely on Windows.
* **Code Change**:

```groovy
// Before:
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
    ...
}

// After:
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

### B. OAuth 2.1 Public Client Registration (`iam-web`)
* **File**: `microservices/iam-service/iam-service-infrastructure/src/main/java/kh/edu/istad/platform/iam/infrastructure/client/ClientSeeder.java`
* **Issue**: Logging into the web console prompted `/oauth2/authorize?client_id=iam-web...`, which responded with `400 Bad Request` ("Whitelabel Error Page") because `iam-web` did not exist in `oauth2_registered_client`.
* **Code Change**:

```java
// Added client seeding logic for the SPA:
@Value("${iam.web.client-id:iam-web}") private String webClientId;
@Value("${iam.web.redirect-uris:http://localhost:9000/auth/callback,http://localhost:5173/auth/callback}") private String webRedirectUris;

@Override
public void run(ApplicationArguments args) {
    seedDcrClient();
    seedWebClient();
}

private void seedWebClient() {
    if (clients.findByClientId(webClientId) != null) return;
    RegisteredClient.Builder b = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId(webClientId)
        .clientName("IAM Web Console")
        .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)          // public SPA client: PKCE instead of secret
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
        .scope(OidcScopes.OPENID)
        .scope(OidcScopes.PROFILE)
        .scope(OidcScopes.EMAIL)
        .scope(OidcScopes.PHONE)
        .scope(OidcScopes.ADDRESS)
        .clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build())
        .tokenSettings(TokenSettings.builder()
            .accessTokenTimeToLive(Duration.ofMinutes(30))
            .refreshTokenTimeToLive(Duration.ofDays(7))
            .reuseRefreshTokens(false)                                         // token rotation
            .build());
    Arrays.stream(webRedirectUris.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(b::redirectUri);
    clients.save(b.build());
}
```

---

### C. Authorization Server CSRF Configuration
* **File**: `microservices/iam-service/iam-service-infrastructure/src/main/java/kh/edu/istad/platform/iam/infrastructure/config/AuthorizationServerConfig.java`
* **Issue**: Authorization server endpoints rejected SPA requests due to missing double-submit CSRF cookie support.
* **Code Change**:

```java
@Bean @Order(1)
SecurityFilterChain authorizationServerChain(HttpSecurity http, LoadUserForAuthenticationUseCase loadUser) throws Exception {
    OAuth2AuthorizationServerConfigurer asc = OAuth2AuthorizationServerConfigurer.authorizationServer();
    // Cookie-based CSRF: SPA consent form posts to /oauth2/authorize with XSRF-TOKEN
    CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
    csrfHandler.setCsrfRequestAttributeName(null);
    http.csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(csrfHandler));
    http.securityMatcher(asc.getEndpointsMatcher())
        ...
```

---

### D. Logout Race Condition & Auto-Login Loop Fix
* **File**: `microservices/iam-service/iam-service-web/frontend/src/auth/AuthContext.jsx`
* **Issue**: Calling `store(null)` inside `logout()` updated React state immediately. The active `<Protected />` component detected `authenticated === false` and ran `login()`, calling `window.location.assign('/oauth2/authorize')`. Because the server session (`JSESSIONID`) had not been cleared yet, the user was instantly logged back in instead of going to `/login`.
* **Code Change**:

```javascript
// Before:
const logout = useCallback(() => {
  store(null); // <-- Triggered Protected re-render -> auto login() loop
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

// After:
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

### E. Spring Security Logout & CSRF Handling
* **File**: `microservices/iam-service/iam-service-infrastructure/src/main/java/kh/edu/istad/platform/iam/infrastructure/config/DefaultSecurityConfig.java`
* **Issue**: 
  1. `POST /logout` without a matching CSRF token returned `403 Forbidden`.
  2. `GET /logout` returned `404 Not Found` because Spring Security defaults to `POST` only when CSRF is enabled.
* **Code Change**:

```java
// Added import:
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

// Updated defaultChain:
http.csrf(c -> c
        .ignoringRequestMatchers("/logout")
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(csrfHandler))
    .authorizeHttpRequests(a -> a
        .requestMatchers("/consent-info").authenticated()
        .anyRequest().permitAll())
    .formLogin(f -> f.loginPage("/login"))
    .oauth2Login(o -> o.loginPage("/login").userInfoEndpoint(u -> u.userService(federated)))
    .logout(l -> l
        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        .logoutSuccessUrl("/login?logout")
        .invalidateHttpSession(true)
        .clearAuthentication(true)
        .deleteCookies("JSESSIONID", "XSRF-TOKEN"))
    .addFilterBefore(new TelegramLoginFilter(botToken, provision, link, load), UsernamePasswordAuthenticationFilter.class);
```

---

## 3. Verification & Live Status

1. **Service Running**: Port `9000` (`http://localhost:9000`).
2. **Login Flow**:
   - Navigating to `http://localhost:9000/login` loads the React console with `XSRF-TOKEN` cookie.
   - Logging in as `admin` executes Authorization Code Flow with PKCE via `iam-web` and successfully redirects to the admin console.
3. **Logout Flow**:
   - Clicking "Sign out" submits `/logout`.
   - Spring Security terminates the session, clears `JSESSIONID` and `XSRF-TOKEN`, and redirects with HTTP 302 to:
     `http://localhost:9000/login?logout`
   - The React app renders the sign-in form with the confirmation badge: `"You have been logged out"`.
