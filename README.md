# ISTAD Microservices - OAuth2 Inter-Service Communication Guide

This project demonstrates secure inter-service communication between **Order Service** and **Customer Service** using **Spring Boot 4**, **Spring Security 7**, **Spring Authorization Server**, **Spring Cloud Consul Discovery & LoadBalancer**, and **Spring Declarative HTTP Interfaces** (`@HttpExchange` with `@ClientRegistrationId`).

---

## Architecture Overview

```
                      +-----------------------------+
                      |   Spring Auth Server        |
                      |   (istad-common-security)   |
                      |   Port: 9000                |
                      +--------------+--------------+
                                     ^
                                     | 1. POST /oauth2/token (client_credentials)
                                     |    Returns: Bearer <JWT>
                                     |
+---------------------+              |              +--------------------------+
|    Order Service    +--------------+              |     Customer Service     |
|   (order-service)   |                             |    (customer-service)    |
|   Port: 30000       +---------------------------->|     Port: 9091           |
+----------+----------+  2. GET /api/customers      +------------+-------------+
           ^                Header: Bearer <JWT>                 |
           |                                                     | 3. GET /oauth2/jwks
           | Client Request                                      v    (Token validation)
           | GET /api/orders/customer               +------------+-------------+
           |                                        |   Spring Auth Server     |
      [Consumer]                                    +--------------------------+
```

### Components

| Service | Module Path | Default Port | Description |
| :--- | :--- | :--- | :--- |
| **Consul** | Docker Container | `8500` | Service discovery and distributed configuration. |
| **Auth Server** | `:istad-common:istad-common-security` | `9000` | Issues OAuth2 JWT tokens (`client_credentials` grant). |
| **Customer Service** | `:microservices:customer-services:customer-service-main` | `9091` | Protected Resource Server requiring `SCOPE_read`. |
| **Order Service** | `:microservices:order-services:order-service-main` | `30000` | Client calling `customer-service` using declarative HTTP client. |

---

## Prerequisites

1. **Java 21+** installed and available on `PATH`.
2. **Consul** running on `localhost:8500`:
   ```bash
   docker run -d --name consul -p 8500:8500 -p 8600:8600/udp consul:latest agent -server -ui -node=server-1 -bootstrap-expect=1 -client=0.0.0.0
   ```
3. **PostgreSQL** running for services that require persistent storage.

---

## Step-by-Step Testing Guide

### Step 1: Start the Authorization Server

In terminal 1:
```powershell
.\gradlew :istad-common:istad-common-security:bootRun
```
- Wait until it logs `Started SecurityApplication` on port **`9000`**.
- Verify endpoint:
  ```powershell
  curl.exe http://localhost:9000/.well-known/openid-configuration
  ```

---

### Step 2: Start Customer Service

In terminal 2:
```powershell
.\gradlew :microservices:customer-services:customer-service-main:bootRun
```
- Wait until it registers with Consul and starts on port **`9091`**.
- Verify registration in Consul at [http://localhost:8500/ui/dc1/services/customer-service](http://localhost:8500/ui/dc1/services/customer-service).

---

### Step 3: Start Order Service

In terminal 3:
```powershell
.\gradlew :microservices:order-services:order-service-main:bootRun
```
- Wait until it starts on port **`30000`** (or `8080` if Consul config is disabled).
- Verify health check:
  ```powershell
  curl.exe -i http://localhost:30000/actuator/health
  ```

---

### Step 4: Verify Customer Service Protection (Direct Calls)

#### 4.1. Test Direct Call WITHOUT Token (Expect 401 Unauthorized)
```powershell
curl.exe -i http://localhost:9091/api/customers
```
**Expected Response:**
```http
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer
```

#### 4.2. Request OAuth2 Token Directly from Auth Server
```powershell
curl.exe -X POST http://localhost:9000/oauth2/token `
  -u "customer-client:customer-secret" `
  -d "grant_type=client_credentials&scope=read write"
```
**Expected Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 299,
  "scope": "read write"
}
```

#### 4.3. Test Direct Call WITH Token (Expect 200 OK)
Replace `<ACCESS_TOKEN>` with the token obtained from 4.2:
```powershell
curl.exe -i -H "Authorization: Bearer <ACCESS_TOKEN>" http://localhost:9091/api/customers
```
**Expected Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "customerId": "4e969de6-088a-4d64-b227-02e352cd0d84",
    "username": "vathanak",
    ...
  }
]
```

---

### Step 5: Test Inter-Service Flow (Order Service -> Customer Service)

Order Service is configured with:
- `@ClientRegistrationId("customer-client")` on the HTTP interface client (`CustomerHttpClient`).
- Automated token retrieval via `OAuth2WebClientHttpServiceGroupConfigurer`.
- Service name resolution via Spring Cloud LoadBalancer (`http://customer-service`).

Execute the request against Order Service:
```powershell
curl.exe -i http://localhost:30000/api/orders/customer
```

**What Happens Behind the Scenes:**
1. `OrderController` receives `GET /api/orders/customer`.
2. It delegates to `CustomerServiceClientAdapter`, which invokes the declarative `CustomerHttpClient.getCustomers()`.
3. The `OAuth2WebClientHttpServiceGroupConfigurer` intercepts the request, notes `@ClientRegistrationId("customer-client")`, and requests a Client Credentials token from `http://127.0.0.1:9000/oauth2/token` (or uses a cached token).
4. The Bearer token is attached: `Authorization: Bearer <token>`.
5. Spring Cloud LoadBalancer resolves `http://customer-service` to `http://localhost:9091`.
6. `customer-service` validates the JWT against `http://localhost:9000/oauth2/jwks` and grants access.
7. `order-service` receives the payload and returns it to the client.

**Expected Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json
Transfer-Encoding: chunked

[
  {
    "customerId": "4e969de6-088a-4d64-b227-02e352cd0d84",
    "username": "vathanak_1791966155453",
    "familyName": "Thai",
    "givenName": "Vathanak",
    "email": "vathanak1791966155453@example.com",
    "phoneNumber": "012345678"
  }
]
```

---

## Configuration Reference

### Order Service Client Configuration (`order-service-client`)

- **HTTP Interface**: [`CustomerHttpClient.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/order-services/order-service-client/src/main/java/kh/edu/istad/platform/order/client/httpClient/CustomerHttpClient.java)
  ```java
  @HttpExchange("/api/customers")
  @ClientRegistrationId("customer-client")
  public interface CustomerHttpClient {
      @GetExchange
      @ClientRegistrationId("customer-client")
      Flux<GetCustomerResult> getCustomers();
  }
  ```

- **OAuth2 Config**: [`OAuth2ClientConfig.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/order-services/order-service-client/src/main/java/kh/edu/istad/platform/order/client/config/OAuth2ClientConfig.java)
  ```java
  @Configuration
  public class OAuth2ClientConfig {
      @Bean
      OAuth2AuthorizedClientManager authorizedClientManager(
              ClientRegistrationRepository repo, OAuth2AuthorizedClientService service) {
          var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(repo, service);
          manager.setAuthorizedClientProvider(
                  OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build());
          return manager;
      }

      @Bean
      WebClientHttpServiceGroupConfigurer oauth2WebClientHttpServiceGroupConfigurer(OAuth2AuthorizedClientManager m) {
          return OAuth2WebClientHttpServiceGroupConfigurer.from(m);
      }
  }
  ```

- **Client Registration & Routing**: [`application.yaml`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/order-services/order-service-main/src/main/resources/application.yaml)
  ```yaml
  spring:
    security:
      oauth2:
        client:
          registration:
            customer-client:
              client-id: customer-client
              client-secret: customer-secret
              authorization-grant-type: client_credentials
              scope:
                - read
                - write
              client-authentication-method: client_secret_basic
          provider:
            customer-client:
              token-uri: http://127.0.0.1:9000/oauth2/token
    http:
      serviceclient:
        customer-service:
          base-url: http://customer-service
  ```

---

## Troubleshooting

| Issue | Cause | Fix |
| :--- | :--- | :--- |
| `503 Service Unavailable` | Consul cannot find healthy instance of `customer-service`. | Ensure `customer-service` is running and its health check at `http://localhost:9091/actuator/health` passes. |
| `401 Unauthorized` on Order Service call | Token not attached by WebClient or Auth server rejecting credentials. | Verify that `OAuth2ClientConfig` is loaded and `token-uri` is reachable. |
| `403 Forbidden` | Token missing required scope `SCOPE_read`. | Verify client scopes in `application.yaml` include `read`. |
