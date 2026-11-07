# IAM Service: Migration, Implementation & Bug Fixes Changelog

A comprehensive technical record of all tasks, architectural migrations, bug fixes, and operational configurations performed on the **`iam-service`** module within `istad-ite-project`.

---

## 📑 Table of Contents
1. [Overview & Scope](#1-overview--scope)
2. [Phase 1: Project Migration (Maven → Gradle)](#2-phase-1-project-migration-maven--gradle)
3. [Phase 2: Comprehensive Package Refactoring](#3-phase-2-comprehensive-package-refactoring)
4. [Phase 3: Database Provisioning & Configuration (`db_iam`)](#4-phase-3-database-provisioning--configuration-db_iam)
5. [Phase 4: Detailed Bug Fixes & Technical Solutions](#5-phase-4-detailed-bug-fixes--technical-solutions)
   - [5.1 Missing Internal Submodule Coordinates](#51-missing-internal-submodule-coordinates)
   - [5.2 Java 25 vs. JDK 21 Lombok Crash](#52-java-25-vs-jdk-21-lombok-crash)
   - [5.3 UTF-8 Byte Order Mark (BOM) Compilation Errors](#53-utf-8-byte-order-mark-bom-compilation-errors)
   - [5.4 Spring Boot 4 BOM & Spring Authorization Server Incompatibility](#54-spring-boot-4-bom--spring-authorization-server-incompatibility)
   - [5.5 Web Starter & Liquibase Artifact Renaming](#55-web-starter--liquibase-artifact-renaming)
   - [5.6 Hibernate `@Table` vs. JPA `@Table` Ambiguity](#56-hibernate-table-vs-jpa-table-ambiguity)
   - [5.7 JTE Circular View Path Error on `/login`](#57-jte-circular-view-path-error-on-login)
6. [Phase 5: Verification & Production Readiness](#6-phase-5-verification--production-readiness)

---

## 1. Overview & Scope

The goal was to migrate the standalone `iam-service` (an OAuth 2.1 / OpenID Connect 1.0 Identity Provider based on Clean Architecture / DDD) into the composite Gradle microservices project `istad-ite-project`, refactor its namespaces to adhere to organization standards, connect it to the central PostgreSQL infrastructure, and get it fully running and verified.

- **Source Codebase**: `D:\ISTAD\microservices\iam-service\iam-service` (Maven Multi-Module)
- **Target Location**: `D:\ISTAD\microservices\istad-ite-project\microservices\iam-service` (Gradle Multi-Project)
- **Target Namespace**: `kh.edu.istad.platform.iam.*`
- **Target Database**: PostgreSQL `db_iam` running on port `9543` in Docker (`ite-postgres`)

---

## 2. Phase 1: Project Migration (Maven → Gradle)

### Submodule Architecture
The original 7 Maven submodules were restructured into Gradle multi-project modules:

```
microservices/iam-service/
├── settings.gradle
├── build.gradle
├── .env.example / .env
├── iam-domain/
│   ├── iam-domain-core/           # Pure business domain (User, Role, Permission)
│   └── iam-application-service/   # Use cases, application ports, commands, and results
├── iam-service-persistence/       # JPA entities, Spring Data repos, MapStruct mappers
├── iam-service-infrastructure/    # Spring Authorization Server, JDBC token stores, token customizers
├── iam-service-restapi/           # REST controllers, DTOs, RFC 9457 Problem Details
├── iam-service-web/               # JTE server-side rendered pages (login, consent, account)
└── iam-service-main/              # Spring Boot entry point, Liquibase master, bootstrap seeders
```

### Build Logic & Version Catalog
1. Registered subproject artifacts in root `gradle/libs.versions.toml`:
   - `iam-core = { module = "kh.edu.istad:iam-domain-core", version.ref = "iam" }`
   - `iam-app-service = { module = "kh.edu.istad:iam-application-service", version.ref = "iam" }`
   - `iam-persistence = { module = "kh.edu.istad:iam-service-persistence", version.ref = "iam" }`
   - `iam-infrastructure = { module = "kh.edu.istad:iam-service-infrastructure", version.ref = "iam" }`
   - `iam-restapi = { module = "kh.edu.istad:iam-service-restapi", version.ref = "iam" }`
   - `iam-web = { module = "kh.edu.istad:iam-service-web", version.ref = "iam" }`

2. Configured root `settings.gradle` and submodule `build.gradle` files to build with Gradle.

---

## 3. Phase 2: Comprehensive Package Refactoring

All Java classes, test cases, and JTE templates were refactored across all 7 submodules from `com.istad.iam.*` to `kh.edu.istad.platform.iam.*`:

| Submodule | Previous Namespace | New Namespace |
| :--- | :--- | :--- |
| `iam-domain-core` | `com.istad.iam.domain.*` | `kh.edu.istad.platform.iam.domain.*` |
| `iam-application-service` | `com.istad.iam.application.*` | `kh.edu.istad.platform.iam.application.*` |
| `iam-service-persistence` | `com.istad.iam.persistence.*` | `kh.edu.istad.platform.iam.persistence.*` |
| `iam-service-infrastructure` | `com.istad.iam.infrastructure.*` | `kh.edu.istad.platform.iam.infrastructure.*` |
| `iam-service-restapi` | `com.istad.iam.rest.*` | `kh.edu.istad.platform.iam.rest.*` |
| `iam-service-web` | `com.istad.iam.web.*` | `kh.edu.istad.platform.iam.web.*` |
| `iam-service-main` | `com.istad.iam.*` | `kh.edu.istad.platform.iam.*` |

- Directories were physically relocated from `src/main/java/com/istad/iam/...` to `src/main/java/kh/edu/istad/platform/iam/...`.
- All `package` declarations and `import` statements were updated across 187 files.
- All `@import` directives in `.jte` templates (`login.jte`, `layout.jte`, `consent.jte`, etc.) were updated to reference the new package paths.

---

## 4. Phase 3: Database Provisioning & Configuration (`db_iam`)

1. **Created Database**:
   - Provisioned `db_iam` inside the containerized PostgreSQL cluster (`ite-postgres`) running on port `9543`.
   - Granted full ownership to user `iteuser`.

2. **Updated DB Initialization Script**:
   - Edited `deployment/postgres/init_db/001_create_db.sql` to include:
     ```sql
     CREATE DATABASE db_iam OWNER iteuser;
     ```
   - Guarantees that future database container recreations automatically include `db_iam`.

3. **Configured Application Datasource**:
   - Updated `application.yml` and `.env` in `microservices/iam-service`:
     ```yaml
     spring:
       datasource:
         url: ${DB_URL:jdbc:postgresql://localhost:9543/db_iam}
         username: ${DB_USERNAME:iteuser}
         password: ${DB_PASSWORD:qwerqwer}
     ```

---

## 5. Phase 4: Detailed Bug Fixes & Technical Solutions

### 5.1 Missing Internal Submodule Coordinates
* **Problem**: Gradle attempted to resolve `kh.edu.istad:iam-domain-core`, `iam-application-service`, etc., as external Maven artifacts and failed with `Could not find kh.edu.istad:iam-*`.
* **Root Cause**: In Gradle multi-project builds, external coordinates are not automatically linked to sibling subprojects unless project dependencies (`project(':...')`) or dependency substitutions are defined.
* **Solution**: In `microservices/iam-service/build.gradle`, configured dependency substitution in `allprojects`:
  ```groovy
  configurations.configureEach {
      resolutionStrategy.dependencySubstitution {
          substitute module('kh.edu.istad:iam-domain-core') using project(':iam-domain:iam-domain-core')
          substitute module('kh.edu.istad:iam-application-service') using project(':iam-domain:iam-application-service')
          substitute module('kh.edu.istad:iam-service-persistence') using project(':iam-service-persistence')
          substitute module('kh.edu.istad:iam-service-infrastructure') using project(':iam-service-infrastructure')
          substitute module('kh.edu.istad:iam-service-restapi') using project(':iam-service-restapi')
          substitute module('kh.edu.istad:iam-service-web') using project(':iam-service-web')
      }
  }
  ```

---

### 5.2 Java 25 vs. JDK 21 Lombok Crash
* **Problem**: Compiling `iam-domain-core` failed with `NoSuchFieldException: com.sun.tools.javac.code.TypeTag :: UNKNOWN` inside `lombok.javac.Javac.<clinit>`.
* **Root Cause**: The shared convention plugins in `build-logic` hardcoded `languageVersion = JavaLanguageVersion.of(25)`. Lombok 1.18.36 crashes when analyzing internal javac AST structures on early Java 25 compilers, whereas the system environment is JDK 21 LTS.
* **Solution**:
  - Enforced Java 21 toolchain in `microservices/iam-service/build.gradle` inside `afterEvaluate`:
    ```groovy
    subprojects {
        afterEvaluate {
            plugins.withType(JavaPlugin).configureEach {
                java {
                    toolchain {
                        languageVersion = JavaLanguageVersion.of(21)
                    }
                }
            }
        }
    }
    ```
  - Upgraded Lombok to `1.18.36` across all modules.

---

### 5.3 UTF-8 Byte Order Mark (BOM) Compilation Errors
* **Problem**: `javac` threw repeated `illegal character: '\ufeff'` compilation errors across multiple modules.
* **Root Cause**: PowerShell file rewrite scripts created files with a leading UTF-8 Byte Order Mark (`0xEF, 0xBB, 0xBF`), which javac treats as illegal characters at column 0.
* **Solution**: Executed a stripping routine using `[System.Text.UTF8Encoding]::new($false)` without BOM across all `.java` files:
  ```powershell
  Get-ChildItem -Filter *.java -Recurse | ForEach-Object {
      $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
      if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
          [System.IO.File]::WriteAllBytes($_.FullName, $bytes[3..($bytes.Length - 1)])
      }
  }
  ```

---

### 5.4 Spring Boot 4 BOM & Spring Authorization Server Incompatibility
* **Problem**: Running `iam-service` crashed on startup with:
  ```
  java.lang.NoClassDefFoundError: org/springframework/security/oauth2/server/authorization/config/annotation/web/configurers/OAuth2AuthorizationServerConfigurer
  ```
* **Root Cause**: `build-logic/src/main/groovy/sping-common-conventions.gradle` imported `org.springframework.boot:spring-boot-dependencies:4.1.1` (Spring Security 7.x). Spring Security 7 refactored Authorization Server classes. However, `iam-service` was developed for Spring Authorization Server 1.4.x / 1.5.x on Spring Boot 3.4.x. The 4.1.1 BOM pulled `spring-security-oauth2-authorization-server-7.1.1.jar`, causing class incompatibilities.
* **Solution**: Created dedicated convention plugins in `build-logic`:
  1. `build-logic/src/main/groovy/iam-spring-common-conventions.gradle`
  2. `build-logic/src/main/groovy/iam-spring-boot-common-conventions.gradle`
  - Both plugins import `org.springframework.boot:spring-boot-dependencies:3.4.1` and enforce Java 21.
  - Substituted the conventions across all `iam-service` modules.

---

### 5.5 Web Starter & Liquibase Artifact Renaming
* **Problem**: Gradle failed dependency resolution with:
  ```
  Could not find org.springframework.boot:spring-boot-starter-webmvc:3.4.1
  Could not find org.springframework.boot:spring-boot-starter-liquibase:3.4.1
  ```
* **Root Cause**: In Spring Boot 3.x, the starter for Spring MVC is `spring-boot-starter-web` (not `webmvc`), and Liquibase integration is provided by `org.liquibase:liquibase-core` with Spring Boot autoconfiguration.
* **Solution**: Updated `iam-service-main/build.gradle`:
  ```groovy
  implementation 'org.springframework.boot:spring-boot-starter-web'
  implementation 'org.liquibase:liquibase-core'
  runtimeOnly 'org.postgresql:postgresql'
  ```

---

### 5.6 Hibernate `@Table` vs. JPA `@Table` Ambiguity
* **Problem**: Entity compilation failed with:
  ```
  error: reference to Table is ambiguous
  both class org.hibernate.annotations.Table in org.hibernate.annotations and class jakarta.persistence.Table in jakarta.persistence match
  ```
* **Root Cause**: Entity source files contained both `import jakarta.persistence.*;` and wildcard `import org.hibernate.annotations.*;`. Both packages define a `@Table` annotation.
* **Solution**:
  - Replaced `import org.hibernate.annotations.*;` with explicit imports:
    ```java
    import org.hibernate.annotations.SQLRestriction;
    import org.hibernate.annotations.JdbcTypeCode;
    ```
  - Standardized on `jakarta.persistence.Table`.

---

### 5.7 JTE Circular View Path Error on `/login`
* **Problem**: Navigating to `http://localhost:9000/login` returned HTTP 500:
  ```
  jakarta.servlet.ServletException: Circular view path [login]: would dispatch back to the current handler URL [/login] again. Check your ViewResolver setup!
  ```
* **Root Cause**: `iam-service-web` renders views using JTE (`login.jte`). In production mode (`use-precompiled-templates: true`), JTE expects precompiled template classes in the classpath. Without the `gg.jte.gradle` plugin, templates were not compiled during the build, causing Spring MVC to fall back to `InternalResourceViewResolver`.
* **Solution**:
  - Applied the official `gg.jte.gradle` plugin in `iam-service-web/build.gradle`:
    ```groovy
    plugins {
        id 'iam-spring-common-conventions'
        id 'gg.jte.gradle' version '3.2.1'
    }

    jte {
        sourceDirectory = file("src/main/jte").toPath()
        generate()
    }
    ```
  - Now, `bootJar` triggers `generateJte` and `compileJava`, embedding precompiled templates (`gg.jte.generated.precompiled.*`) directly inside the JAR.

---

### 5.8 Missing Default for DCR Client Secret (`clientSeeder` BeanCreationException)
* **Problem**: Running without environment variables failed with:
  ```
  org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'clientSeeder': Injection of autowired dependencies failed
  ```
* **Root Cause**: In `ClientSeeder.java`, `@Value("${iam.dcr.client-secret}")` and `application.yml`'s `${IAM_DCR_CLIENT_SECRET}` had no default fallback value. When started directly from an IDE or without `.env` exported into the shell session, Spring Boot failed to resolve the placeholder.
* **Solution**:
  - In `ClientSeeder.java`: Added `@Value("${iam.dcr.client-secret:change-me-too}")`.
  - In `application.yml`: Added `${IAM_DCR_CLIENT_SECRET:change-me-too}` and defaults for `IAM_ADMIN_*`. Now runs out-of-the-box with zero required environment variables.

---

## 6. Phase 5: Verification & Production Readiness

### Verified Functionality
1. **Gradle Build**:
   - `bootJar` builds cleanly and packages all 7 modules into `iam-service-main-1.0.0.jar`.
2. **Database Migrations**:
   - Liquibase executed all changelogs against `db_iam` (`localhost:9543`), creating 12 schema tables and constraint indexes.
3. **Application Boot & Seeding**:
   - `Tomcat started on port 9000 (http) with context path '/'`.
   - `AdminSeeder` created initial administrator: `admin` (`admin@istad.co`).
4. **Live HTTP Verification**:
   - **OIDC Discovery**: `GET http://localhost:9000/.well-known/openid-configuration` returns `200 OK` JSON metadata.
   - **JWKS Endpoint**: `GET http://localhost:9000/oauth2/jwks` returns `200 OK` RSA public keys.
   - **Login UI (JTE SSR)**: `GET http://localhost:9000/login` returns `200 OK` HTML rendered page.
