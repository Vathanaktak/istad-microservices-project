# 📋 IAM Service Presentation & Demo Script (Page-by-Page)

> **Project:** Identity & Access Management (IAM) Service  
> **Tech Stack:** Spring Boot 3.4.1, Spring Authorization Server, React 19 (Vite), PostgreSQL, Liquibase, Hexagonal Architecture  
> **Target Audience:** Presentation guide for demonstrating the system page-by-page to instructors or evaluators.

---

## 📑 Presentation Table of Contents

1. **System Introduction & Architecture Overview**
2. **Part 1: Authentication & Security Flows**
   - 1.1. Login Page (`/login`)
   - 1.2. Registration Page (`/register`)
   - 1.3. Password Recovery (`/forgot-password` & `/reset-password`)
   - 1.4. OAuth2 Consent Screen (`/oauth2/consent`)
3. **Part 2: User Account Portal**
   - 2.1. Profile Management (`/account/profile`)
   - 2.2. Account Security & Password (`/account/security`)
   - 2.3. Linked Social Accounts (`/account/linked`)
   - 2.4. Active Sessions (`/account/sessions`)
   - 2.5. Login History & Audit (`/account/login-history`)
   - 2.6. Account Activity Timeline (`/account/activity`)
   - 2.7. Account Deactivation Request (`/account/deactivation`)
4. **Part 3: Administrator Management Console**
   - 3.1. Admin Overview Dashboard (`/admin`)
   - 3.2. User Management & User Details (`/admin/users` & `/admin/users/:id`)
   - 3.3. Deactivation Review Queue (`/admin/deactivations`)
   - 3.4. Roles & Permissions Management (`/admin/roles` & `/admin/permissions`)
   - 3.5. OAuth2 Registered Clients (`/admin/clients`)
   - 3.6. Global Login Monitor (`/admin/login-history`)
   - 3.7. Global System Audit Logs (`/admin/audit-logs`)
   - 3.8. System Configuration (`/admin/system`)
5. **Teacher Q&A Defense Tips**

---

## 0. Introduction & Architecture Overview

### 🎙️ Presenter Script:
> *"Good morning/afternoon, teachers and evaluators. Today I am presenting our **IAM (Identity & Access Management) Service**, which serves as the centralized Single Sign-On (SSO) and Identity Provider for the ISTAD microservices ecosystem.*
>
> *Key technical highlights of this project include:*
> - *A **Spring Boot 3.4.1** backend powered by **Spring Authorization Server (OAuth 2.1 & OpenID Connect 1.0)**, structured strictly according to **Hexagonal (Ports and Adapters)** architecture.*
> - *A modern **React 19 / Vite Single Page Application (SPA)** embedded directly into the executable JAR, supporting bilingual localization (English & Khmer).*
> - *A **PostgreSQL** persistence layer versioned with **Liquibase** migrations.*
>
> *I will now walk through the system page-by-page to demonstrate each functional workflow."*

---

## Part 1: Authentication Flows

### 1.1. Login Page (`/login`)
- **URL:** `http://localhost:9000/login`
- **Actions to Demo:**
  1. Show login form: Username/Email, Password, "Remember Me", and Language selector (EN / KM).
  2. Show Social Login options: Google, GitHub, and Telegram Login widget.
  3. Enter an incorrect password once to demonstrate real-time error handling.
  4. Sign in with valid credentials.
- **🎙️ Presenter Script:**
  > *"This is the **Login Page**. It supports standard password credentials as well as Federated Social Login (Google, GitHub, and Telegram). If a user inputs wrong credentials, Spring Security's `LoginAttemptListener` captures the client's IP and User-Agent, records the failed attempt in PostgreSQL, and increments the account lockout counter to safeguard against brute-force attacks."*
- **💡 Technical Note:** Uses OAuth 2.1 Authorization Code Flow with PKCE (`S256`).

---

### 1.2. Registration Page (`/register`)
- **URL:** `http://localhost:9000/register`
- **Actions to Demo:**
  1. Click "Create an account" on the login page.
  2. Highlight the input fields: Username, Email, First Name, Last Name, Password, Confirm Password.
  3. Show the dynamic password strength indicators.
- **🎙️ Presenter Script:**
  > *"The **Registration Page** registers new users. The backend validates format rules for usernames, emails, and enforces strong password criteria. Passwords are encrypted using BCrypt (`BCryptPasswordEncoder`). Upon successful registration, the domain service assigns the default role `ROLE_USER` via `RegisterUserUseCase`."*

---

### 1.3. Password Recovery (`/forgot-password` & `/reset-password`)
- **URL:** `http://localhost:9000/forgot-password` and `http://localhost:9000/reset-password`
- **Actions to Demo:**
  1. Open `/forgot-password`, enter a registered email, and submit.
  2. Explain how the time-bound token is hashed and persisted in `password_reset_tokens`.
- **🎙️ Presenter Script:**
  > *"For lost credentials, the **Password Recovery** workflow generates a cryptographically secure random token with a 15-minute expiration. To protect against database compromise, tokens are hashed before storage. When the user submits a new password at `/reset-password`, existing reset tokens for that user are immediately invalidated, and an audit entry is created."*

---

### 1.4. OAuth2 Consent Screen (`/oauth2/consent`)
- **URL:** `http://localhost:9000/oauth2/consent`
- **Actions to Demo:**
  1. Display the OAuth2 scope authorization screen (shown when third-party client apps connect).
  2. Highlight requested scopes: `openid`, `profile`, `email`, `offline_access`.
- **🎙️ Presenter Script:**
  > *"This page implements the **OAuth 2.1 Consent Screen** per RFC 6749 and OIDC specifications. When external applications request access to a user's account, this screen asks for explicit user consent before issuing an authorization code."*

---

## Part 2: User Account Portal

*(Sign in as a normal user or admin to access the Account portal)*

### 2.1. Profile Management (`/account/profile`)
- **URL:** `http://localhost:9000/account/profile`
- **Actions to Demo:**
  1. Show profile information: Avatar, Full Name, Email, Phone, Gender, Date of Birth, Address.
  2. Edit a field (e.g., Nickname or Phone Number) and click Save.
  3. Point out the success notification toast.
- **🎙️ Presenter Script:**
  > *"On the **Profile Page**, users manage their personal details. Updates are processed through `UpdateUserProfileUseCase` and domain entity validation. Every profile update automatically creates an audit entry (`PROFILE_UPDATED`) in `audit_logs`."*

---

### 2.2. Account Security (`/account/security`)
- **URL:** `http://localhost:9000/account/security`
- **Actions to Demo:**
  1. Show the password change form: Current Password, New Password, Confirm New Password.
  2. Explain social-only users: Users who signed up via Google or GitHub without a password can set one directly without inputting a current password.
- **🎙️ Presenter Script:**
  > *"The **Security Page** allows users to change their password securely. The backend validates the old password against the BCrypt hash before applying the new hash, recording a `PASSWORD_CHANGED` audit record."*

---

### 2.3. Linked Accounts (`/account/linked`)
- **URL:** `http://localhost:9000/account/linked`
- **Actions to Demo:**
  1. View linked identity providers (Google, GitHub, Telegram).
  2. Show "Connected" badges and the "Unlink" action.
  3. Explain the protective business rule: **A user cannot unlink their last sign-in method**.
- **🎙️ Presenter Script:**
  > *"The **Linked Accounts Page** manages federated social identities. To protect account access, `UnlinkIdentityUseCase` enforces a critical business rule: if a user has no local password and only one social provider, unlinking is blocked so the user is never locked out of their account."*

---

### 2.4. Active Sessions (`/account/sessions`)
- **URL:** `http://localhost:9000/account/sessions`
- **Actions to Demo:**
  1. Show the list of devices and browsers signed in to the account.
  2. Point out the "This device" badge on the current session.
  3. Show the "Sign out" button on individual sessions and the "Sign out all other sessions" button.
- **🎙️ Presenter Script:**
  > *"The **Active Sessions Page** displays all active devices and client sessions connected to the account. Session timestamps and metadata are queried from Spring Authorization Server via `SessionAdapter`. Users can remotely terminate any session or terminate all other devices simultaneously."*

---

### 2.5. Login History (`/account/login-history`)
- **URL:** `http://localhost:9000/account/login-history`
- **Actions to Demo:**
  1. View the audit table of login attempts: Status (Success / Failed), IP Address, Device, Browser, OS, and Timestamp.
  2. Filter by status (All, Success, Failed).
- **🎙️ Presenter Script:**
  > *"The **Login History Page** gives users visibility into all authentication attempts on their account. Every sign-in attempt captures the client IP, user agent, failure reason (e.g., bad credentials or locked account), and flags suspicious activity."*

---

### 2.6. Account Activity Timeline (`/account/activity`)
- **URL:** `http://localhost:9000/account/activity`
- **Actions to Demo:**
  1. View the security timeline: Password changes, profile edits, session terminations, and deactivation proposals.
  2. Show corresponding action icons, IP addresses, and relative timestamps ("x minutes ago").
- **🎙️ Presenter Script:**
  > *"The **Account Activity Page** serves as a personal audit trail. Security-critical changes made to the user's account are logged with actor details, timestamps, and client IP addresses."*

---

### 2.7. Account Deactivation Request (`/account/deactivation`)
- **URL:** `http://localhost:9000/account/deactivation`
- **Actions to Demo:**
  1. Open the deactivation request form.
  2. Select a reason (e.g., Privacy concerns, No longer using) and enter optional details.
  3. Type the confirmation keyword `"DEACTIVATE"` and submit.
  4. Show the yellow banner indicating: *"Your request is being reviewed by an administrator"*.
  5. Show that the user can still use their account and can click "Cancel request" at any time.
- **🎙️ Presenter Script:**
  > *"The **Account Deactivation Page** implements our Enterprise Proposal-Approval workflow. Users submit a formal deactivation proposal with an explanation instead of deleting their account abruptly. While pending review, the user can continue using the system or cancel the request."*

---

## Part 3: Administrator Management Console

*(Sign in as `admin` to access the Admin section)*

### 3.1. Admin Overview (`/admin`)
- **URL:** `http://localhost:9000/admin`
- **Actions to Demo:**
  1. View summary metric cards: Total Users, Active Sessions, Pending Deactivations, and Recent Sign-ins.
- **🎙️ Presenter Script:**
  > *"The **Admin Overview** provides system health indicators, active sessions, and highlights actionable queues like pending deactivations."*

---

### 3.2. User Management & User Details (`/admin/users` & `/admin/users/:id`)
- **URL:** `http://localhost:9000/admin/users`
- **Actions to Demo:**
  1. View the paginated table of system users with search and filter capabilities.
  2. Click on a user who has a pending deactivation request.
  3. Notice the **Deactivation Proposal Banner** at the top with quick "Approve" and "Reject" buttons.
  4. Show administrative actions: Enable/Disable account, Lock/Unlock, Reset Password, and Role assignment.
- **🎙️ Presenter Script:**
  > *"In **User Management**, administrators can inspect and modify user accounts. When opening a user with a pending deactivation proposal, an alert banner displays the user's reason and provides immediate review actions."*

---

### 3.3. Deactivation Review Queue (`/admin/deactivations`)
- **URL:** `http://localhost:9000/admin/deactivations`
- **Actions to Demo:**
  1. Navigate to the dedicated Deactivations queue.
  2. Click "Review" on a pending proposal.
  3. Enter administrative review notes in the review modal.
  4. Click **Approve & Deactivate**:
     - The user is automatically disabled (`enabled = false`).
     - All active sessions for that user are immediately terminated.
     - The proposal status changes to `PROCESSED`.
     - An audit record is logged with the reviewer's username and notes.
- **🎙️ Presenter Script:**
  > *"The **Deactivations Queue** is a dedicated administrative workflow. Approving a proposal triggers `ApproveDeactivationRequestUseCase`, disabling the user, invalidating all their active sessions, and recording a comprehensive audit trail."*

---

### 3.4. Roles & Permissions (`/admin/roles` & `/admin/permissions`)
- **URL:** `http://localhost:9000/admin/roles` and `/admin/permissions`
- **Actions to Demo:**
  1. Show system roles (`ROLE_ADMIN`, `ROLE_USER`) and their mapped permissions.
  2. Show granular permissions (`user:read`, `user:write`, `role:read`, `client:read`, `audit:read`).
- **🎙️ Presenter Script:**
  > *"We enforce **Role-Based Access Control (RBAC)**. Roles are composed of granular permissions which are mapped into JWT claims (`authorities` and `scope`), allowing downstream microservices to verify access rights independently."*

---

### 3.5. OAuth2 Registered Clients (`/admin/clients`)
- **URL:** `http://localhost:9000/admin/clients`
- **Actions to Demo:**
  1. Show registered OAuth2 clients (e.g., `web-client`, `mobile-app`).
  2. Display Client ID, Redirect URIs, Grant Types, and Scopes.
  3. Demonstrate client secret regeneration.
- **🎙️ Presenter Script:**
  > *"The **Clients Page** manages external client registrations per RFC 6749, controlling redirect URIs, allowed grant types, and client secrets."*

---

### 3.6. Global Login Monitor (`/admin/login-history`)
- **URL:** `http://localhost:9000/admin/login-history`
- **Actions to Demo:**
  1. View system-wide sign-in attempts across all accounts.
  2. Filter by username or failure status.
- **🎙️ Presenter Script:**
  > *"The **Global Login Monitor** gives administrators visibility into all authentication attempts across the platform to detect coordinated attacks or anomalies."*

---

### 3.7. Global Audit Logs (`/admin/audit-logs`)
- **URL:** `http://localhost:9000/admin/audit-logs`
- **Actions to Demo:**
  1. View the platform-wide audit log table: Action, Actor, Target, IP Address, Details, and Timestamp.
- **🎙️ Presenter Script:**
  > *"The **Audit Logs Page** provides immutable records of administrative and security events for compliance and security auditing."*

---

### 3.8. System Configuration (`/admin/system`)
- **URL:** `http://localhost:9000/admin/system`
- **Actions to Demo:**
  1. View dynamic system configuration keys: Password policies, lockout thresholds, and session timeouts.
- **🎙️ Presenter Script:**
  > *"The **System Configuration** page allows administrators to adjust runtime security policies without restarting the service."*

---

## 4. Teacher Q&A Defense Tips

### Question 1: Why did you choose Hexagonal Architecture (Ports and Adapters)?
> **Answer:**  
> *"Hexagonal Architecture decouples core business logic from frameworks and external technologies. Our domain core and use cases do not depend on Spring MVC or JPA repositories. All interactions happen through outbound ports (interfaces). This makes the application testable, maintainable, and allows swapping databases or infrastructure without altering core domain rules."*

---

### Question 2: How does the system defend against Brute-Force Password attacks?
> **Answer:**  
> *"We listen to Spring Security's `AuthenticationFailureBadCredentialsEvent` in `LoginAttemptListener`. Every failed attempt is recorded in `login_history`, and `RecordLoginAttemptUseCase` increments the account failure count. Once the failure threshold is exceeded, the account is temporarily locked via `locked_until` or permanently locked until an administrator intervenes."*

---

### Question 3: How are tokens stored and managed in the React frontend?
> **Answer:**  
> *"We follow defense-in-depth principles:
> 1. The **Access Token** is stored in `sessionStorage` and cleared when the browser tab closes.
> 2. The **Refresh Token** is stored strictly **in memory only** and never written to storage, mitigating persistent token theft through Cross-Site Scripting (XSS).
> 3. The server session is protected by an `HttpOnly`, `SameSite=Lax` cookie (`JSESSIONID`), preventing client-side JavaScript access."*

---

### Question 4: Why use a Proposal-Approval workflow for Deactivations instead of immediate deletion?
> **Answer:**  
> *"In enterprise and financial systems, immediate hard-deletion can compromise data integrity and break historical audit trails. The proposal-approval pattern allows administrators to review requests, verify pending obligations, soft-disable the account, terminate all active sessions, and retain an audit record of who reviewed and approved the deactivation."*

---

## 🏁 Presentation Conclusion

> *"In conclusion, our **IAM Service** delivers an enterprise-grade identity solution combining RFC-standard OAuth 2.1 / OIDC protocols, Hexagonal Architecture, comprehensive audit logging, and responsive user interfaces. 
> 
> Thank you for your time and attention. I look forward to any questions or feedback."*
