# Buf Build Implementation & gRPC Architecture Guide

This guide documents the implementation of **Buf Build** within `istad-common/istad-common-grpc`, explaining what was configured, how it works, the core purpose of using Buf in modern microservices architectures, and how to test everything.

---

## 🎯 Purpose of Buf Build

[Buf](https://buf.build) is a modern, fast, and developer-friendly toolchain designed to manage Protocol Buffers (Protobuf) and gRPC APIs.

In enterprise and polyglot microservice environments, Buf solves key challenges that traditional `protoc` workflows struggle with:

| Capability | Traditional `protoc` Workflow | Buf Build Toolchain |
| :--- | :--- | :--- |
| **Plugin Installation** | Requires manually downloading & compiling OS-dependent binaries (`protoc`, `protoc-gen-go`, `protoc-gen-grpc-java`). | **Remote Plugins**: Uses Buf Schema Registry (BSR) to generate code in the cloud with zero local binary setup. |
| **Linting & Best Practices** | No built-in linting; developers frequently introduce inconsistent casing, field naming, and package paths. | **Built-in Linter (`buf lint`)**: Enforces strict API design standards (Google Protobuf conventions, snake_case fields, PascalCase messages). |
| **Breaking Change Detection** | Difficult to spot breaking changes before deployment; risks downtime between microservices. | **Breaking Change Detector (`buf breaking`)**: Compares schemas directly against Git branches (e.g. `main`) or tags before merging. |
| **Polyglot Code Generation** | Complex shell scripts with dozens of `--proto_path` and `--plugin` arguments for each language. | **Declarative Generation (`buf generate`)**: Single `buf.gen.yaml` config generates Java, Go, Python, TypeScript stubs simultaneously. |
| **Language Options** | Protos cluttered with language-specific options (`option go_package`, `option java_package`). | **Managed Mode**: Dynamically generates language-specific package paths without polluting shared `.proto` definitions. |

---

## 🏗️ What Was Implemented

### 1. File Structure Reorganization

Previously, `buf.yaml` and `buf.gen.yaml` were located in `src/main/`. They were moved to the root of the `istad-common-grpc` module so commands can be executed directly from the module directory:

```
istad-common/istad-common-grpc/
├── buf.yaml               # Buf v2 module definition & lint rules
├── buf.gen.yaml           # Polyglot generation config (Java, Go, Python)
├── build.gradle           # Gradle build file for Java/Spring Boot microservices
├── README.md              # Module-level documentation
└── src/
    └── main/
        └── proto/
            └── product.proto  # gRPC schema definitions
```

---

### 2. Module & Lint Configuration (`buf.yaml`)

Path: `istad-common/istad-common-grpc/buf.yaml`

```yaml
version: v2

modules:
  - path: src/main/proto

lint:
  use:
    - STANDARD
  except:
    # Allowed exceptions to preserve backwards compatibility with existing Java microservices
    - PACKAGE_DIRECTORY_MATCH
    - PACKAGE_VERSION_SUFFIX
    - RPC_REQUEST_RESPONSE_UNIQUE
    - RPC_REQUEST_STANDARD_NAME
    - RPC_RESPONSE_STANDARD_NAME

breaking:
  use:
    - FILE
```

* **`modules.path`**: Points to `src/main/proto` where the Protobuf source files are located.
* **`lint.except`**: While Buf's `STANDARD` rules enforce strict conventions (such as mandatory `v1` versioning and 1:1 request/response messages), we added exceptions so that existing Java services (`product-service` and `order-service-client`) continue working seamlessly without requiring breaking code changes.

---

### 3. Polyglot Code Generation (`buf.gen.yaml`)

Path: `istad-common/istad-common-grpc/buf.gen.yaml`

```yaml
version: v2

managed:
  enabled: true
  override:
    - file_option: go_package_prefix
      value: kh.edu.istad/gen/go

plugins:
  # Java Protobuf & gRPC
  - remote: buf.build/protocolbuffers/java
    out: gen/java
  - remote: buf.build/grpc/java
    out: gen/java

  # Go Protobuf & gRPC
  - remote: buf.build/protocolbuffers/go
    out: gen/go
    opt:
      - paths=source_relative
  - remote: buf.build/grpc/go
    out: gen/go
    opt:
      - paths=source_relative

  # Python Protobuf & gRPC
  - remote: buf.build/protocolbuffers/python
    out: gen/python
  - remote: buf.build/grpc/python
    out: gen/python
```

* **`managed.enabled: true`**: Automatically supplies `go_package_prefix` during Go generation.
* **`remote:` plugins**: Executes compilation on the Buf Schema Registry without requiring local tool installations.
* **`out: gen/...`**: Separates generated stubs by language:
  * `gen/java/`: Java stubs and classes.
  * `gen/go/`: Go struct models and gRPC client/server interfaces.
  * `gen/python/`: Python modules and gRPC stubs.

---

### 4. Protobuf Schema Update (`product.proto`)

Path: `istad-common/istad-common-grpc/src/main/proto/product.proto`

Added `option go_package` to guarantee Go builds work correctly out of the box:

```protobuf
syntax = "proto3";

package kh.edu.istad.product.grpc;

option java_multiple_files = true;
option java_package = "kh.edu.istad.product.grpc";
option java_outer_classname = "ProductProto";
option go_package = "kh.edu.istad/platform/product/grpc;productgrpc";

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

---

### 5. Git & Gradle Configuration

* **Git Ignore**: Added `gen/` to `.gitignore` at the project root so that locally generated polyglot stubs are not committed to source control.
* **Gradle Coexistence**: The existing `com.google.protobuf` Gradle plugin in `build.gradle` continues to compile Java classes directly into `build/generated/source/proto/` during `./gradlew build`, ensuring continuous compatibility with all Spring Boot microservices.

---

## 🔄 Dual Workflow: Gradle vs. Buf Build

```
                       ┌────────────────────────────┐
                       │  src/main/proto/*.proto    │
                       └─────────────┬──────────────┘
                                     │
                 ┌───────────────────┴───────────────────┐
                 │                                       │
                 ▼                                       ▼
      [ Gradle Build Workflow ]                [ Buf Build Toolchain ]
    ./gradlew :istad-common-grpc:build              buf generate
                 │                                       │
                 ▼                                       ▼
       build/generated/...                             gen/
                 │                             ├── java/ (Java Stubs)
                 ▼                             ├── go/   (Go Stubs)
    istad-common-grpc-1.0.0.jar                └── python/ (Python Stubs)
                 │
                 ▼
       Consumed by Spring Boot:
       - product-service
       - order-services
```

---

## 🧪 Step-by-Step Testing Guide

### Part 1: Testing the Buf Build Toolchain

You can test Buf Build in two ways: via **Docker** (requires no local CLI installation) or via the **Buf CLI**.

#### Option A: Using Docker (Instant, No Installation Required)

Open PowerShell and navigate to the root directory:

1. **Test `buf build` (Verify syntax & compilation)**:
   ```powershell
   docker run --rm -v "${PWD}/istad-common/istad-common-grpc:/workspace" -w /workspace bufbuild/buf:latest build
   ```
   *Expected result*: Command exits with code `0` and no errors.

2. **Test `buf lint` (Verify API style & lint rules)**:
   ```powershell
   docker run --rm -v "${PWD}/istad-common/istad-common-grpc:/workspace" -w /workspace bufbuild/buf:latest lint
   ```
   *Expected result*: Command exits with code `0` cleanly.

3. **Test `buf generate` (Generate Go, Java, Python stubs)**:
   ```powershell
   docker run --rm -v "${PWD}/istad-common/istad-common-grpc:/workspace" -w /workspace bufbuild/buf:latest generate
   ```
   *Expected result*: Code is generated into `istad-common/istad-common-grpc/gen/`:
   * `gen/go/product.pb.go` & `product_grpc.pb.go`
   * `gen/java/.../ProductGrpcServiceGrpc.java`, etc.
   * `gen/python/product_pb2.py` & `product_pb2_grpc.py`

---

#### Option B: Using the Native Buf CLI

1. **Install the CLI** (choose one):
   ```powershell
   # Via Windows Package Manager (Winget):
   winget install bufbuild.buf

   # Or via Scoop:
   scoop install buf
   ```

2. **Run tests from the module folder**:
   ```powershell
   cd D:\ISTAD\microservices\istad-ite-project\istad-common\istad-common-grpc

   buf build       # Compiles and validates schemas
   buf lint        # Lints schemas against rules
   buf generate    # Generates Java, Go, Python stubs into gen/
   ```

---

### Part 2: Testing Microservices Compilation (Gradle)

Verify that the Java microservices compile cleanly with the updated Proto module:

1. **Build `istad-common-grpc`**:
   ```powershell
   cd D:\ISTAD\microservices\istad-ite-project
   .\gradlew.bat :istad-common:istad-common-grpc:build
   ```
   *Expected result*: `BUILD SUCCESSFUL`.

2. **Compile `product-service`**:
   ```powershell
   .\gradlew.bat :microservices:product-service:classes
   ```
   *Expected result*: `BUILD SUCCESSFUL` (confirms gRPC server implementations compile without errors).

3. **Compile `order-services`**:
   ```powershell
   .\gradlew.bat :microservices:order-services:order-service-client:classes
   ```
   *Expected result*: `BUILD SUCCESSFUL` (confirms gRPC client adapters compile without errors).

---

### Part 3: End-to-End Runtime Test (gRPC Communication)

1. **Start Infrastructure**:
   ```powershell
   docker start ite-postgres
   ```

2. **Run `product-service` (gRPC Server on port 9095)**:
   ```powershell
   .\gradlew.bat :microservices:product-service:bootRun
   ```

3. **Verify gRPC Server using `grpcurl` or Postman**:
   ```powershell
   # If grpcurl is installed:
   grpcurl -plaintext localhost:9095 list
   # Expected output:
   # grpc.reflection.v1alpha.ServerReflection
   # kh.edu.istad.product.grpc.ProductGrpcService
   ```

4. **Run `order-service` (gRPC Client) and test HTTP invocation**:
   ```powershell
   .\gradlew.bat :microservices:order-services:order-service-client:bootRun
   ```
   Send an HTTP request through Postman or cURL:
   ```bash
   curl -X GET http://localhost:30000/api/orders/products/<PRODUCT_ID>
   ```
   The client will invoke `ProductGrpcService.GetProductById` over gRPC (port 9095) and return the product details.
