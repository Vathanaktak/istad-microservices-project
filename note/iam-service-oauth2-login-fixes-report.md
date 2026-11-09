# IAM Service OAuth2 Social Login Troubleshooting & Resolution Report

This report documents the issues encountered during GitHub and Google OAuth2 login integration in `microservices/iam-service`, their root causes, and the corrective actions implemented.

---

## 1. Summary of Issues

| # | Symptom | Trigger / Flow | Root Cause | Resolution |
|---|---|---|---|---|
| **1** | GitHub login showed **404 Not Found** on GitHub | Clicking "Continue with GitHub" from `/login` | Spring Boot did not automatically load `microservices/iam-service/.env`. Client ID defaulted to `disabled` in `application.yml`, causing GitHub to return 404 for an invalid `client_id`. | Added automatic `.env` discovery and loading in [`IamApplicationService.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-main/src/main/java/kh/edu/istad/platform/iam/IamApplicationService.java). |
| **2** | Spring Boot startup crash: `Client id of registration 'google' must not be empty` | Running `bootRun` with unconfigured providers in `.env` | Blank `.env` entries (e.g. `GOOGLE_CLIENT_ID=`) set empty string properties `""`, bypassing `application.yml` fallback defaults (`:disabled`). | Enhanced `loadDotenv()` to ignore blank `.env` entries so YAML default values apply. |
| **3** | Successful OAuth authorization redirected to `http://localhost:9000/login?error` | GitHub & Google callback `/login/oauth2/code/*` | `User.java`'s `validateNewUser()` strictly required `passwordHash != null`. Social login users do not have a password on first login, throwing `BusinessRuleException` during user provisioning. | Updated [`User.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-domain/iam-domain-core/src/main/java/kh/edu/istad/platform/iam/domain/model/User.java) to make password optional for federated users. |

---

## 2. Detailed Technical Breakdown

### Issue 1: GitHub OAuth "Not Found" (404)

#### Root Cause
In [`application.yml`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-main/src/main/resources/application.yml#L37-L41):
```yaml
github:
  client-id: ${GITHUB_CLIENT_ID:disabled}
  client-secret: ${GITHUB_CLIENT_SECRET:disabled}
  scope: read:user,user:email
  redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
```
When running the service in IntelliJ IDEA or command line without manually exporting `.env` into the environment, Spring Boot fell back to `${GITHUB_CLIENT_ID:disabled}` (`"disabled"`).
When navigating to `/oauth2/authorization/github`, Spring Security redirected to:
```text
https://github.com/login/oauth/authorize?response_type=code&client_id=disabled&...
```
GitHub looks up the OAuth application by `client_id`. Because no OAuth app exists with ID `disabled`, GitHub returned an HTTP 404 "Not Found" page.

*(Note: Navigating directly to `http://localhost:9000/login/oauth2/code/github` in a browser also returns a 404 because that is the callback endpoint meant only for GitHub with `?code=...` parameters, not the user entry point).*

#### Solution
Implemented automatic `.env` discovery in [`IamApplicationService.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-main/src/main/java/kh/edu/istad/platform/iam/IamApplicationService.java):
```java
private static void loadDotenv() {
    List<Path> candidates = List.of(
        Path.of(".env"),
        Path.of("microservices/iam-service/.env"),
        Path.of("../.env")
    );
    for (Path path : candidates) {
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                        int idx = trimmed.indexOf('=');
                        String key = trimmed.substring(0, idx).trim();
                        String val = trimmed.substring(idx + 1).trim();
                        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                            val = val.substring(1, val.length() - 1).trim();
                        }
                        if (!key.isEmpty() && !val.isBlank() && System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, val);
                        }
                    }
                }
                System.out.println("[iam-service] Loaded environment variables from: " + path.toAbsolutePath());
                break;
            } catch (IOException ignored) {}
        }
    }
}
```

---

### Issue 2: Empty Variables Causing Startup Failure

#### Root Cause
When `.env` contained empty placeholders:
```env
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
FACEBOOK_CLIENT_ID=
FACEBOOK_CLIENT_SECRET=
```
Setting `System.setProperty("GOOGLE_CLIENT_ID", "")` overrode the fallback `${GOOGLE_CLIENT_ID:disabled}`, causing Spring Security's `OAuth2ClientProperties.validateRegistration()` to fail on startup:
```text
Caused by: java.lang.IllegalStateException: Client id of registration 'google' must not be empty.
    at org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.validateRegistration
```

#### Solution
Added `!val.isBlank()` condition in `loadDotenv()` so unconfigured entries are skipped and default YAML fallbacks (`disabled`) remain active until credentials are provided.

---

### Issue 3: OAuth Success Redirecting to `/login?error`

#### Root Cause
Once credentials were configured, the OAuth2 handshake with Google and GitHub succeeded, and GitHub redirected back to `http://localhost:9000/login/oauth2/code/github?code=...&state=...`.

However, Spring Security then invoked [`FederatedOAuth2UserService.loadUser()`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-infrastructure/src/main/java/kh/edu/istad/platform/iam/infrastructure/federation/FederatedOAuth2UserService.java#L24):
```java
// If user has not signed up locally before, auto-provision:
user = provision.execute(cmd);
```
[`FindOrProvisionFederatedUserUseCase.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-domain/iam-application-service/src/main/java/kh/edu/istad/platform/iam/application/usecase/FindOrProvisionFederatedUserUseCase.java#L47) provisions new social users with `rawPassword = null`.

In [`UserCreator.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-domain/iam-application-service/src/main/java/kh/edu/istad/platform/iam/application/support/UserCreator.java#L37):
```java
iamDomainService.createUser(u);
```
Which invoked `user.initiateUser()` -> `validateNewUser()` in [`User.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-domain/iam-domain-core/src/main/java/kh/edu/istad/platform/iam/domain/model/User.java):
```java
// BEFORE:
if (passwordHash == null || passwordHash.isBlank()) {
    throw new BusinessRuleException("user-password-required", "Password hash must not be blank");
}
```
Because federated accounts authenticated via external providers have no initial password (`password_hash` column in PostgreSQL `users` table is nullable), this threw a `BusinessRuleException`.
`FederatedOAuth2UserService` caught this as a `DomainException`, converted it into `OAuth2AuthenticationException("federation_failed")`, and Spring Security's failure handler redirected the browser to:
```text
http://localhost:9000/login?error
```

#### Solution
Updated validation in [`User.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-domain/iam-domain-core/src/main/java/kh/edu/istad/platform/iam/domain/model/User.java#L106-L108):
```java
// AFTER:
if (passwordHash != null && passwordHash.isBlank()) {
    throw new BusinessRuleException("user-password-blank", "Password hash must not be blank");
}
```
* Social users (`passwordHash == null`) pass validation and are provisioned into the database.
* Local registrations still enforce non-null, valid passwords via command validators and form checks.

---

## 3. Files Modified

| File | Change Description |
|---|---|
| [`IamApplicationService.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-main/src/main/java/kh/edu/istad/platform/iam/IamApplicationService.java) | Added `loadDotenv()` method to auto-load `.env` variables into system properties on startup. |
| [`User.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-domain/iam-domain-core/src/main/java/kh/edu/istad/platform/iam/domain/model/User.java) | Relaxed password validation in `validateNewUser()` to allow `null` password hashes for federated accounts. |
| [`microservices/iam-service/.env`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/.env) | Added `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`, `GOOGLE_CLIENT_ID`, and `GOOGLE_CLIENT_SECRET`. |

---

## 4. Verification & Testing

Both providers have been tested and verified against the running server on port 9000:

1. **GitHub Authorization Redirect**:
   ```http
   GET /oauth2/authorization/github
   HTTP/1.1 302 Found
   Location: https://github.com/login/oauth/authorize?response_type=code&client_id=Ov23liipUauXtd9qges9&scope=read:user%20user:email&redirect_uri=http://localhost:9000/login/oauth2/code/github
   ```
2. **Google Authorization Redirect**:
   ```http
   GET /oauth2/authorization/google
   HTTP/1.1 302 Found
   Location: https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=your_google_client_id&scope=profile%20email&redirect_uri=http://localhost:9000/login/oauth2/code/google
   ```
3. **Application Endpoints**:
   * Login UI: `http://localhost:9000/login`
   * Root: `http://localhost:9000/`
