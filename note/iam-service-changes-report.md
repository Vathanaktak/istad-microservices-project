# IAM Service Refactoring & Changes Report

This report summarizes all modifications and additions performed in `microservices/iam-service` based on current `git status`.

---

## 1. Git Status Overview

```text
Changes not staged for commit:
  M microservices/iam-service/iam-domain/iam-application-service/src/main/java/kh/edu/istad/platform/iam/application/support/UserCreator.java
  M microservices/iam-service/iam-domain/iam-application-service/src/main/java/kh/edu/istad/platform/iam/application/usecase/UpdateUserProfileUseCase.java
  M microservices/iam-service/iam-domain/iam-domain-core/src/main/java/kh/edu/istad/platform/iam/domain/model/Permission.java
  M microservices/iam-service/iam-domain/iam-domain-core/src/main/java/kh/edu/istad/platform/iam/domain/model/Role.java
  M microservices/iam-service/iam-domain/iam-domain-core/src/main/java/kh/edu/istad/platform/iam/domain/model/User.java
  M microservices/iam-service/iam-service-persistence/src/main/java/kh/edu/istad/platform/iam/persistence/mapper/UserEntityMapper.java

Untracked files:
  ?? microservices/iam-service/iam-domain/iam-domain-core/src/main/java/kh/edu/istad/platform/iam/domain/event/
  ?? microservices/iam-service/iam-domain/iam-domain-core/src/main/java/kh/edu/istad/platform/iam/domain/valueobject/
```

---

## 2. New Components Added

### A. Value Objects (`kh.edu.istad.platform.iam.domain.valueobject`)
Introduced Domain-Driven Design (DDD) immutable value objects to encapsulate validation, formatting, and domain logic:

| Value Object | Type | Description |
|---|---|---|
| `PhoneNumber.java` | `record` | Stores and encapsulates E.164 phone numbers (e.g. `+85512345678`). |
| `Email.java` | `record` | Automatically normalizes and lowercases email strings. |
| `Username.java` | `record` | Trims and encapsulates usernames. |
| `PersonName.java` | `record` | Encapsulates composite name attributes: `givenName`, `familyName`, `middleName`, `nickname`, and provides `.fullName()`. |
| `Address.java` | `record` | Encapsulates OIDC standard address components: `streetAddress`, `locality`, `region`, `postalCode`, `country`. |
| `RoleName.java` | `record` | Encapsulates role identifier and provides authority derivation (`ROLE_<name>`). |
| `PermissionName.java` | `record` | Encapsulates composite permission strings formatted as `resource:action` (e.g. `user:read`). |

---

### B. Domain Events (`kh.edu.istad.platform.iam.domain.event`)
Created a standalone domain event hierarchy implementing `DomainEvent<T>` without hard coupling to external modules:

| Event Record | Aggregate | Purpose / Trigger |
|---|---|---|
| `DomainEvent.java` | Generic | Base marker interface for IAM domain events. |
| `UserCreatedEvent.java` | `User` | Fired when a new user registers or is provisioned by admin. |
| `UserUpdatedEvent.java` | `User` | Fired when profile information is updated. |
| `UserDeletedEvent.java` | `User` | Fired when an account is soft-deleted. |
| `UserLockedEvent.java` | `User` | Fired on administrative lock or excessive failed logins. |
| `UserUnlockedEvent.java` | `User` | Fired when a locked account is restored. |
| `UserEnabledChangedEvent.java` | `User` | Fired when an account is enabled or disabled. |
| `UserPasswordChangedEvent.java` | `User` | Fired when password is reset or changed. |
| `RoleCreatedEvent.java` | `Role` | Fired when a new role definition is added. |
| `RoleUpdatedEvent.java` | `Role` | Fired on role updates. |
| `RoleDeletedEvent.java` | `Role` | Fired when a role is removed. |
| `PermissionCreatedEvent.java` | `Permission` | Fired when a new permission is registered. |
| `PermissionUpdatedEvent.java` | `Permission` | Fired on permission modifications. |
| `PermissionDeletedEvent.java` | `Permission` | Fired when a permission is removed. |

---

## 3. Existing Files Modified

### 1. `User.java` (`iam-domain-core`)
- Replaced flat primitive strings with Domain Value Objects:
  - `username` (`Username`)
  - `email` (`Email`)
  - `phoneNumber` (`PhoneNumber`)
  - `name` (`PersonName`)
  - `address` (`Address`)
- Retained backward-compatible String getters (`getUsername()`, `getEmail()`, `getGivenName()`, `getPhoneNumber()`, etc.) and setters so existing services, persistence adapters, and MapStruct mappers continue functioning without breaking changes.

### 2. `Role.java` (`iam-domain-core`)
- Switched `name` field to `RoleName`.
- Retained `getName()` and `setName(String)` delegating to `RoleName.of(v)`.
- Delegated `authority()` logic to `RoleName.authority()`.

### 3. `Permission.java` (`iam-domain-core`)
- Switched `name` field to `PermissionName`.
- Updated `nameOf(resource, action)` to use `PermissionName.of(resource, action).value()`.

### 4. `UserEntityMapper.java` (`iam-service-persistence`)
- Configured MapStruct mapping expressions:
  - Flat `UserEntity` columns (`givenName`, `familyName`, etc.) -> `PersonName` value object.
  - Flat `UserEntity` columns (`addressStreetAddress`, `addressLocality`, etc.) -> `Address` value object.
  - Exposed explicit mapping targets from `User` to `UserEntity` for entity updates.

### 5. `UserCreator.java` (`iam-application-service`)
- Updated user instantiation to populate `Address` value object on user creation.

### 6. `UpdateUserProfileUseCase.java` (`iam-application-service`)
- Replaced individual address field mutations with immutable `Address` value object reconstruction, preserving existing attributes if unchanged.
