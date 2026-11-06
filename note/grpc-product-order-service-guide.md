# ISTAD Microservices Architecture & gRPC Integration Guide

This repository contains the microservices suite for the ISTAD platform, including Eureka/Consul discovery, shared domain/client libraries in `istad-common`, and service-to-service communication using **gRPC** and **REST**.

---

## 🏗️ Architecture & Communication Flow

```
[ Client / Browser / Postman ]
             │
             │ HTTP (GET /api/orders/products/{id})
             ▼
┌────────────────────────────────────────────────────────┐
│               order-service (Port 30000)               │
│                                                        │
│  OrderController                                       │
│        │                                               │
│  ProductServiceClient (Domain Port Interface)          │
│        │                                               │
│  ProductGrpcClientAdapter                              │
│        │                                               │
│  CorrelationIdClientInterceptor (x-correlation-id)     │
└──────────────────────────┬─────────────────────────────┘
                           │
                           │ gRPC / Protobuf (Port 9095)
                           ▼
┌────────────────────────────────────────────────────────┐
│              product-service (Port 8085)               │
│                                                        │
│  ProductGrpcServerImpl (Netty gRPC Port 9095)          │
│        │                                               │
│  ProductService                                        │
│        │                                               │
│  ProductRepository (Spring Data JPA)                   │
└──────────────────────────┬─────────────────────────────┘
                           │
                           │ JDBC
                           ▼
               ┌───────────────────────┐
               │ PostgreSQL (Port 9543)│
               │ Database: product_db  │
               └───────────────────────┘
```

---

## 📋 Prerequisites

* **Java JDK**: Version 21 or newer.
* **Docker Desktop**: For running PostgreSQL (`ite-postgres`) and Consul containers.
* **Gradle Wrapper**: Bundled in the repository (`./gradlew` or `.\gradlew.bat`).

---

## 🚀 Step-by-Step Setup & Execution Guideline

### Step 1: Start PostgreSQL Infrastructure

Make sure the PostgreSQL container is running on port `9543`.

```powershell
# From the deployment directory:
cd D:\ISTAD\microservices\istad-ite-project\deployment\postgres
docker compose up -d
```

Verify that the databases `product_db` and `db_order` exist. If needed, create them:

```powershell
docker exec -i ite-postgres psql -U iteuser -d postgres -c "CREATE DATABASE product_db;"
docker exec -i ite-postgres psql -U iteuser -d postgres -c "CREATE DATABASE db_order;"
```

---

### Step 2: Shared gRPC & Protobuf Module (`istad-common-grpc`)

1. **Protobuf Schema Definition**:
   The shared Protobuf definition is located at:
   `istad-common/istad-common-grpc/src/main/proto/product.proto`

   ```protobuf
   syntax = "proto3";
   package kh.edu.istad.product.grpc;

   option java_multiple_files = true;
   option java_package = "kh.edu.istad.product.grpc";

   service ProductGrpcService {
     rpc GetProductById (ProductRequest) returns (ProductResponse);
     rpc CreateProduct (CreateProductRequest) returns (ProductResponse);
   }

   message ProductRequest {
     string product_id = 1;
   }

   message CreateProductRequest {
     string name = 1;
     double price = 2;
     int32 quantity = 3;
   }

   message ProductResponse {
     string product_id = 1;
     string name = 2;
     double price = 3;
     int32 quantity = 4;
   }
   ```

2. **Build the Shared gRPC Stubs**:
   ```powershell
   cd D:\ISTAD\microservices\istad-ite-project
   .\gradlew.bat :istad-common:istad-common-grpc:build
   ```

---

### Step 3: Product Service (`product-service`)

`product-service` acts as the gRPC server and provides local REST CRUD endpoints:

* **Configuration**: `microservices/product-service/src/main/resources/application.yaml`
  * HTTP Port: `8085`
  * gRPC Server Port: `9095`
  * Database: `jdbc:postgresql://localhost:9543/product_db`

* **Run `product-service`**:
  ```powershell
  cd D:\ISTAD\microservices\istad-ite-project
  .\gradlew.bat :microservices:product-service:bootRun
  ```

---

### Step 4: Order Service (`order-services`)

`order-services` communicates with `product-service` using the gRPC client adapter.

1. **gRPC Target Configuration**:
   In `microservices/order-services/order-service-main/src/main/resources/application.yaml`:
   ```yaml
   grpc:
     client:
       product-service:
         address: 'static://localhost:9095'
         negotiation-type: plaintext
   ```

2. **Client Implementation**:
   * `ProductGrpcClientAdapter`: Injects `@GrpcClient("product-service")` and translates gRPC `Status` codes (e.g., `NOT_FOUND` to HTTP `404`).
   * `CorrelationIdClientInterceptor`: Appends a UUID to `x-correlation-id` metadata for distributed request tracing.

3. **Exposed Endpoint**:
   * `GET /api/orders/products/{id}` in `OrderController`.

4. **Run `order-services`**:
   ```powershell
   cd D:\ISTAD\microservices\istad-ite-project
   .\gradlew.bat :microservices:order-services:order-service-main:bootRun
   ```

---

## 🧪 Verification & Testing

### 1. Create a Product (via product-service REST API)

```powershell
$body = @{ name = "MacBook Pro M3"; price = 1999.99; quantity = 10 } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8085/api/v1/products" -Method Post -ContentType "application/json" -Body $body
```

**Expected Response**:
```json
{
  "id": "7c86efe1-d0a1-4605-a5a8-d4f1e43491a3",
  "name": "MacBook Pro M3",
  "price": 1999.99,
  "quantity": 10
}
```

---

### 2. Query Product via order-service (gRPC Inter-Service Communication)

Call `order-service` on port `30000`, which queries `product-service` via gRPC on port `9095`:

```powershell
Invoke-RestMethod -Uri "http://localhost:30000/api/orders/products/7c86efe1-d0a1-4605-a5a8-d4f1e43491a3" -Method Get
```

**Expected Response**:
```json
{
  "id": "7c86efe1-d0a1-4605-a5a8-d4f1e43491a3",
  "name": "MacBook Pro M3",
  "price": 1999.99,
  "quantity": 10
}
```

---

### 3. Verify Error Handling (404 Not Found)

Test querying a non-existent product through `order-service`:

```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:30000/api/orders/products/non-existent-id" -Method Get
} catch {
    Write-Host "HTTP Status:" $_.Exception.Response.StatusCode.value__
}
```

**Expected Output**:
```
HTTP Status: 404
```

---

## 📌 Ports & Services Overview

| Service / Component | Protocol | Port | Description |
| :--- | :--- | :--- | :--- |
| **product-service** (HTTP) | HTTP | `8085` | Product REST controllers & Swagger UI |
| **product-service** (gRPC) | gRPC | `9095` | High-performance Netty gRPC server |
| **order-service** | HTTP | `30000` | Order API & gRPC client to `product-service` |
| **ite-postgres** | TCP | `9543` | PostgreSQL (`product_db`, `db_order`, etc.) |
| **Consul Server** | HTTP | `8500` | Service discovery and configuration |

---

## 🛠️ Project Structure Summary

```
istad-ite-project/
├── build-logic/                          # Gradle convention plugins
├── deployment/                           # Docker compose files for PostgreSQL, Consul, Vault
├── gradle/
│   └── libs.versions.toml                # Dependency versions (gRPC, Spring Boot, Lombok)
├── istad-common/
│   ├── istad-common-domain/              # Shared domain objects
│   ├── istad-common-grpc/                # Protobuf specs and generated gRPC classes
│   ├── istad-common-restapi/             # Shared REST response models
│   ├── istad-common-swagger/             # OpenAPI / Swagger configuration
│   └── istad-common-webflux/             # WebFlux utilities
└── microservices/
    ├── order-services/
    │   ├── order-service-client/         # ProductGrpcClientAdapter & interceptors
    │   ├── order-service-domain/         # Ports (ProductServiceClient) and DTOs
    │   ├── order-service-main/           # Spring Boot application runner & YAML config
    │   └── order-service-restapi/        # OrderController exposing /api/orders/products/{id}
    └── product-service/                  # ProductServiceApplication & ProductGrpcServerImpl
```
