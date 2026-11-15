# 📋 ស្គ្រីបបទបង្ហាញគម្រោង IAM Service (Demo Script Page-by-Page)

> **គម្រោង៖** Identity & Access Management (IAM) Service  
> **បច្ចេកវិទ្យា៖** Spring Boot 3.4.1, Spring Authorization Server, React 19 (Vite), PostgreSQL, Liquibase, Hexagonal Architecture  
> **គោលបំណង៖** ឯកសារណែនាំសម្រាប់សិស្ស/និស្សិត យកទៅនិយាយ និងបង្ហាញជូនលោកគ្រូ/អ្នកគ្រូតាមលំដាប់លំដោយពីទំព័រមួយទៅទំព័រមួយ (Page-by-Page)។

---

## 📑 តារាងមាតិកានៃការធ្វើ Demo (Presentation Outline)

1. **សេចក្តីផ្តើម និងស្ថាបត្យកម្មរួម (System Architecture Overview)**
2. **ផ្នែកទី ១៖ លំហូរផ្ទៀងផ្ទាត់អត្តសញ្ញាណ (Authentication & Security Flows)**
   - ២.១. ទំព័រចូលគណនី (Login Page - `/login`)
   - ២.២. ទំព័រចុះឈ្មោះ (Register Page - `/register`)
   - ២.៣. ទំព័រភ្លេច និងផ្លាស់ប្តូរពាក្យសម្ងាត់ (Password Recovery - `/forgot-password` & `/reset-password`)
   - ២.៤. ទំព័រយល់ព្រមផ្ទេរសិទ្ធិ OAuth2 (OAuth2 Consent - `/oauth2/consent`)
3. **ផ្នែកទី ២៖ ផតថលគ្រប់គ្រងគណនីអ្នកប្រើប្រាស់ (User Account Portal)**
   - ៣.១. ទំព័រប្រវត្តិរូប (Profile - `/account/profile`)
   - ៣.២. ទំព័រសុវត្ថិភាព (Security - `/account/security`)
   - ៣.៣. ទំព័រភ្ជាប់គណនីបណ្តាញសង្គម (Linked Accounts - `/account/linked`)
   - ៣.៤. ទំព័រសម័យកំពុងដំណើរការ (Active Sessions - `/account/sessions`)
   - ៣.៥. ទំព័រប្រវត្តិការចូល (Login History - `/account/login-history`)
   - ៣.៦. ទំព័រសកម្មភាពគណនី (Account Activity - `/account/activity`)
   - ៣.៧. ទំព័រស្នើសុំបិទគណនី (Account Deactivation - `/account/deactivation`)
4. **ផ្នែកទី ៣៖ ផ្ទាំងគ្រប់គ្រងប្រព័ន្ធរដ្ឋបាល (Admin Management Console)**
   - ៤.១. ផ្ទាំងទិដ្ឋភាពរួម (Admin Overview - `/admin`)
   - ៤.២. គ្រប់គ្រងអ្នកប្រើប្រាស់ (User Management - `/admin/users` & `/admin/users/:id`)
   - ៤.៣. ពិនិត្យសំណើសុំបិទគណនី (Deactivation Review - `/admin/deactivations`)
   - ៤.៤. គ្រប់គ្រងតួនាទី និងសិទ្ធិ (Roles & Permissions - `/admin/roles` & `/admin/permissions`)
   - ៤.៥. គ្រប់គ្រងអតិថិជន OAuth2 (OAuth2 Clients - `/admin/clients`)
   - ៤.៦. តាមដានការចូលប្រព័ន្ធសរុប (Global Login Monitor - `/admin/login-history`)
   - ៤.៧. កំណត់ត្រាសវនកម្មប្រព័ន្ធ (Audit Logs - `/admin/audit-logs`)
   - ៤.៨. ការកំណត់ប្រព័ន្ធ (System Config - `/admin/system`)
5. **គន្លឹះឆ្លើយសំណួរការពារមុខគ្រូ (Teacher Q&A Defense Tips)**

---

## ០. សេចក្តីផ្តើម (Opening & Architecture)

### 🎙️ ពាក្យនិយាយណែនាំខ្លួន និងគម្រោង៖
> *"ជម្រាបសួរលោកគ្រូ/អ្នកគ្រូ។ ថ្ងៃនេះខ្ញុំបាទ/នាងខ្ញុំសូមធ្វើការ demo បង្ហាញពីគម្រោង **IAM Service (Identity and Access Management)** ដែលជា Single Sign-On (SSO) & Centralized Identity Provider សម្រាប់ប្រព័ន្ធ Microservices របស់ ISTAD។*
>
> *គម្រោងនេះត្រូវបានបង្កើតឡើងដោយ៖*
> - *Backend ប្រើ **Spring Boot 3.4.1** រួមជាមួយ **Spring Authorization Server (OAuth 2.1 & OpenID Connect 1.0)** និងរៀបចំតាមស្តង់ដារ **Hexagonal Architecture (Ports & Adapters)**។*
> - *Frontend ប្រើ **React 19 / Vite** ដែលជា Single Page Application (SPA) មានភ្ជាប់មកជាមួយ i18n ២ ភាសា (ភាសាខ្មែរ និងអង់គ្លេស)។*
> - *Database ប្រើ **PostgreSQL** និងគ្រប់គ្រង Schema ដោយ **Liquibase**។*
> 
> *ឥឡូវនេះខ្ញុំសូមអនុញ្ញាតបង្ហាញមុខងារទំព័រនីមួយៗតាមលំដាប់លំដោយបាទ/ចាស។"*

---

## ផ្នែកទី ១៖ លំហូរផ្ទៀងផ្ទាត់អត្តសញ្ញាណ (Authentication Flows)

### ១.១. ទំព័រចូលគណនី (Login Page)
- **URL:** `http://localhost:9000/login`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញទម្រង់ Login: Username/Password, Remember me checkbox, ជម្រើសប្តូរភាសា (ខ្មែរ/English)។
  2. បង្ហាញប៊ូតុង Social Login: Google, GitHub, Telegram។
  3. សាកល្បងវាយ Password ខុស ១ ដង ដើម្បីបង្ហាញ Error message។
  4. ចូលដោយគណនី Admin ឬ User ត្រឹមត្រូវ។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"ទំព័រដំបូងនេះគឺជា **Login Page**។ ប្រព័ន្ធទ្រទ្រង់ទាំងការចូលដោយ Username/Email និង Password ធម្មតា ព្រមទាំង Federated Social Login (Google, GitHub, Telegram Login Widget)។ ប្រសិនបើ User វាយ Password ខុស ប្រព័ន្ធនឹងកត់ត្រាការបរាជ័យ (Failed Attempt) ចូលទៅក្នុង Database ភ្លាមៗដើម្បីតាមដាន Brute-force។ លើសពីនេះ UI គាំទ្រភាសាខ្មែរពេញលេញ ដែលអាចប្តូរបាននៅជ្រុងខាងលើ។"*
- **💡 ចំណុចបច្ចេកទេស (Highlight to Teacher):**
  - Spring Security Form Login បញ្ជូនតាម OAuth2 Authorization Code Flow + PKCE (`S256`).
  - Event listener `LoginAttemptListener` ចាប់យក IP Address និង User-Agent ស្វ័យប្រវត្តិ។

---

### ១.២. ទំព័រចុះឈ្មោះ (Register Page)
- **URL:** `http://localhost:9000/register`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. ចុចលើតំណភ្ជាប់ *"Create an account / បង្កើតគណនី"* នៅលើទំព័រ Login។
  2. បង្ហាញទម្រង់ Input: Username, Email, First Name, Last Name, Password, Confirm Password។
  3. បង្ហាញ Password Strength Meter (កម្រិតសុវត្ថិភាពពាក្យសម្ងាត់)។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"នេះជាទំព័រ **Register** សម្រាប់សិស្ស ឬអ្នកប្រើប្រាស់ថ្មី។ នៅទីនេះមាន Validation យ៉ាងម៉ត់ចត់ទាំងលើ Username format, Email format និងកម្រិតសុវត្ថិភាព Password (ត្រូវមានអក្សរធំ តូច លេខ និងនិមិត្តសញ្ញា)។ នៅពេលចុះឈ្មោះជោគជ័យ ប្រព័ន្ធនឹង assign តួនាទីជា `ROLE_USER` ដោយស្វ័យប្រវត្តិតាមរយៈ `RegisterUserUseCase`។"*
- **💡 ចំណុចបច្ចេកទេស:**
  - Password ត្រូវបាន hash ដោយ `BCryptPasswordEncoder`។
  - រៀបចំតាម DDD: Entity `User` គ្រប់គ្រង Business logic ផ្ទាល់ខ្លួន។

---

### ១.៣. ទំព័រភ្លេច និងកំណត់ពាក្យសម្ងាត់ឡើងវិញ (Password Recovery)
- **URL:** `http://localhost:9000/forgot-password` និង `http://localhost:9000/reset-password`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បើកទំព័រ `/forgot-password` ហើយវាយ Email របស់អ្នកប្រើប្រាស់។
  2. ចុចប៊ូតុងបញ្ជូន -> ប្រព័ន្ធបង្ហាញសារថាបានផ្ញើតំណភ្ជាប់ Reset។
  3. ពន្យល់ពី Token Reset ដែលត្រូវបានបង្កើតក្នុង DB (តារាង `password_reset_tokens`)។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"ក្នុងករណីអ្នកប្រើប្រាស់ភ្លេចពាក្យសម្ងាត់ ពួកគេអាចស្នើសុំ Reset Password បាន។ ប្រព័ន្ធនឹង generate Secure Random Token ដែលមានសុពលភាពកំណត់ (ឧទាហរណ៍ 15 នាទី) ហើយ Hash រក្សាទុកក្នុង Database ដោយមិនទុក Plain Token ឡើយ។ នៅពេល User ប្រើ Token នោះមកប្តូរ Password ថ្មីនៅទំព័រ `/reset-password` Token ចាស់ៗទាំងអស់នឹងត្រូវ invalidated ភ្លាមៗ ហើយព្រឹត្តិការណ៍នេះត្រូវបានកត់ចូលសវនកម្ម (Audit Log)។"*

---

### ១.៤. ទំព័រយល់ព្រមសិទ្ធិ OAuth2 (Consent Page)
- **URL:** `http://localhost:9000/oauth2/consent`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញទំព័រ Consent (ពេលមាន Third-party Client ដូចជា Web LMS ឬ Mobile App ស្នើសុំសិទ្ធិចូលប្រើ)។
  2. បង្ហាញ Scopes ដូចជា: `openid`, `profile`, `email`, `offline_access`។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"នេះជាទំព័រ **OAuth2 Consent** ដែលស្របតាមស្តង់ដារ RFC 6749 និង OpenID Connect។ នៅពេល Client Application ណាមួយចង់ស្នើសុំទិន្នន័យ User ទំព័រនេះនឹងលោតឡើងសួរការយល់ព្រមពីម្ចាស់គណនីមុនពេលចេញ Authorization Code។"*

---

## ផ្នែកទី ២៖ ផតថលគ្រប់គ្រងគណនីផ្ទាល់ខ្លួន (User Account Portal)

*(ចំណាំ៖ សូម Login ចូលដោយគណនីធម្មតា ឬ Admin រួចបង្ហាញ Sidebar ខាងឆ្វេងក្រោមផ្នែក "Account / គណនី")*

### ២.១. ទំព័រប្រវត្តិរូប (Profile Page)
- **URL:** `http://localhost:9000/account/profile`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញព័ត៌មានផ្ទាល់ខ្លួន៖ រូបភាពតំណាង (Avatar), ឈ្មោះពេញ, អ៊ីមែល, លេខទូរស័ព្ទ, ភេទ, ថ្ងៃខែឆ្នាំកំណើត, និងអាសយដ្ឋាន។
  2. សាកល្បងកែសម្រួលព័ត៌មាន (ឧ. ប្តូរ Nickname ឬ Phone) រួចចុច Save។
  3. បង្ហាញ Toast notification ជោគជ័យ។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"នេះជាទំព័រ **Profile** ផ្ទាល់ខ្លួនរបស់អ្នកប្រើប្រាស់។ User អាចធ្វើបច្ចុប្បន្នភាពទិន្នន័យផ្ទាល់ខ្លួនបានដូចជា ឈ្មោះ លេខទូរស័ព្ទ ភេទ និងអាសយដ្ឋាន។ រាល់ពេលមានការកែប្រែ ប្រព័ន្ធដំណើរការតាមរយៈ `UpdateUserProfileUseCase` និងកត់ត្រាសវនកម្ម `PROFILE_UPDATED` ទៅកាន់ `audit_logs` ភ្លាមៗ។"*

---

### ២.២. ទំព័រសុវត្ថិភាព (Security Page)
- **URL:** `http://localhost:9000/account/security`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញទម្រង់ប្តូរពាក្យសម្ងាត់៖ Current Password, New Password, Confirm New Password។
  2. ពន្យល់ករណី Social User ដែលមិនទាន់មានពាក្យសម្ងាត់ (អាច Set password ថ្មីដោយមិនបាច់វាយ Current Password)។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"ទំព័រ **Security** នេះប្រើសម្រាប់ផ្លាស់ប្តូរពាក្យសម្ងាត់។ ប្រព័ន្ធនឹងផ្ទៀងផ្ទាត់ Password ចាស់ជាមួយ BCrypt hash។ ប្រសិនបើត្រឹមត្រូវ វានឹង update password ថ្មី និងបង្កើត Audit Log ប្រភេទ `PASSWORD_CHANGED`។"*

---

### ២.៣. ទំព័រភ្ជាប់គណនីបណ្តាញសង្គម (Linked Accounts)
- **URL:** `http://localhost:9000/account/linked`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញបញ្ជី Social Providers (Google, GitHub, Telegram)។
  2. បង្ហាញ Badge "Connected / បានភ្ជាប់" ឬប៊ូតុង "Connect / ភ្ជាប់"។
  3. ពន្យល់ពីប៊ូតុង Unlink (ផ្តាច់គណនី)។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"ទំព័រ **Linked Accounts** អនុញ្ញាតឱ្យ User ភ្ជាប់ ឬផ្តាច់គណនី Social Login របស់ខ្លួន។ យើងមាន Business Rule ការពារមួយដ៏សំខាន់គឺ៖ **ហាមផ្តាច់ (Unlink) វិធីសាស្ត្រចូលគណនីចុងក្រោយ** (Cannot unlink the last sign-in method) ដើម្បីកុំឱ្យ User បាត់បង់សិទ្ធិចូលគណនីរបស់ខ្លួន។"*
- **💡 ចំណុចបច្ចេកទេស:**
  - ផ្ទៀងផ្ទាត់ដោយ `UnlinkIdentityUseCase` និងមាន Unit Test គ្របដណ្តប់ ១០០%។

---

### ២.៤. ទំព័រសម័យកំពុងដំណើរការ (Active Sessions)
- **URL:** `http://localhost:9000/account/sessions`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញឧបករណ៍ដែលកំពុង Login (Device type, Browser, OS, IP Address)។
  2. បង្ហាញ Badge "This device / ឧបករណ៍នេះ" សម្រាប់ Session បច្ចុប្បន្ន។
  3. បង្ហាញប៊ូតុង "Sign out / ចាកចេញ" លើ Session ផ្សេង ឬប៊ូតុង "Sign out all other sessions"។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"ទំព័រ **Active Sessions** បង្ហាញរាល់ឧបករណ៍ទាំងអស់ដែលកំពុង Login លើគណនីនេះ។ ទិន្នន័យនេះត្រូវបានទាញចេញពី Spring Authorization Server តាមរយៈ `SessionAdapter`។ User អាចដកសិទ្ធិ ឬ Force Sign Out ឧបករណ៍ណាដែលសង្ស័យបានគ្រប់ពេល។"*
- **💡 ចំណុចបច្ចេកទេស:**
  - គណនាពេលវេលាបង្កើត និង Last active ដោយប្រើ `COALESCE(authorization_code_issued_at, access_token_issued_at, ...)`។
  - ពេលចុច Sign out ប្រព័ន្ធដំណើរការ `TerminateSessionUseCase` និងកត់ត្រា `SESSION_TERMINATED`។

---

### ២.៥. ទំព័រប្រវត្តិការចូល (Login History)
- **URL:** `http://localhost:9000/account/login-history`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញតារាង Login History: ស្ថានភាព (ជោគជ័យ / បរាជ័យ), ទីតាំង/IP Address, ឧបករណ៍, Browser, OS, ពេលវេលា។
  2. បង្ហាញប៊ូតុង Filter: All, Success, Failed។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"នេះជាទំព័រ **Login History**។ រាល់ការប៉ុនប៉ងចូលគណនី (ទាំងជោគជ័យ និងបរាជ័យ) ត្រូវបានកត់ត្រាចូលក្នុងតារាង `login_history` ដោយមានទាំងព័ត៌មានលម្អិតពីមូលហេតុបរាជ័យ (ដូចជា `bad_credentials`, `locked`) និងទង់សម្គាល់ការចូលគួរឱ្យសង្ស័យ (Suspicious flag) ជួយឱ្យម្ចាស់គណនីតាមដានសុវត្ថិភាពរបស់ខ្លួនបានច្បាស់លាស់។"*

---

### ២.៦. ទំព័រសកម្មភាពគណនី (Account Activity)
- **URL:** `http://localhost:9000/account/activity`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញ Timeline សកម្មភាពសំខាន់ៗដែលទើបតែកើតឡើង (ឧទាហរណ៍៖ Profile details updated, Password changed, Session ended, Deactivation requested)។
  2. បង្ហាញ Icon ជាក់លាក់ទៅតាមប្រភេទសកម្មភាពនីមួយៗ និង IP Address ព្រមទាំងពេលវេលា ("x minutes ago")។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"ទំព័រ **Account Activity** ដើរតួជា Security Audit Timeline សម្រាប់ម្ចាស់គណនីផ្ទាល់។ រាល់សកម្មភាពសំខាន់ៗដែលពាក់ព័ន្ធនឹងសុវត្ថិភាព ដូចជាការប្តូរ Password, ការកែ Profile, ការផ្តាច់ Social Account, ឬការស្នើសុំបិទគណនី ត្រូវបានបង្ហាញនៅទីនេះទាំងអស់តាមលំដាប់លំដោយពេលវេលា។"*

---

### ២.៧. ទំព័រស្នើសុំបិទគណនី (Account Deactivation)
- **URL:** `http://localhost:9000/account/deactivation`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញទម្រង់ជ្រើសរើសមូលហេតុ (Reason dropdown)៖ មិនប្រើទៀត, បញ្ហាឯកជនភាព, មានគណនីផ្សេង, ឬផ្សេងៗ។
  2. បង្ហាញប្រអប់ពិពណ៌នាបន្ថែម (Details)។
  3. បង្ហាញប្រអប់វាយពាក្យបញ្ជាក់ `"DEACTIVATE"` ដើម្បីការពារការចុចច្រឡំ។
  4. សាកល្បងចុច Request -> បង្ហាញ Banner ពណ៌លឿងថា *"សំណើរបស់អ្នកកំពុងរង់ចាំការពិនិត្យពី Admin (Pending review)"*។
  5. បង្ហាញប៊ូតុង "Cancel request" ករណី User ចង់បោះបង់ការស្នើសុំវិញ។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"នេះជាលក្ខណៈពិសេសថ្មីដែលយើងទើបបានរៀបចំឡើងគឺ **Account Deactivation Proposal Workflow**។ ដើម្បីការពារសុវត្ថិភាព និងទប់ស្កាត់ការបាត់បង់ទិន្នន័យ ប្រព័ន្ធមិនអនុញ្ញាតឱ្យ User លុបគណនីខ្លួនឯងភ្លាមៗដោយគ្មានការត្រួតពិនិត្យឡើយ។ User ត្រូវផ្ញើសំណើ (Proposal) ទៅកាន់ Administrator។ ខណៈពេលរង់ចាំការអនុម័ត User នៅតែអាចប្រើប្រាស់គណនីបានធម្មតា ឬអាចបោះបង់សំណើ (Cancel request) វិញបានគ្រប់ពេល។"*

---

## ផ្នែកទី ៣៖ ផ្ទាំងគ្រប់គ្រងប្រព័ន្ធរដ្ឋបាល (Admin Management Console)

*(ចំណាំ៖ សូមប្រើគណនីដែលមាន Role `ROLE_ADMIN` ដូចជា `admin` ដើម្បីបង្ហាញផ្នែកនេះ)*

### ៣.១. ផ្ទាំងទិដ្ឋភាពរួម Admin (Admin Overview)
- **URL:** `http://localhost:9000/admin`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញកាតស្ថិតិសំខាន់ៗ (Metric Cards)៖ Total Users, Active Sessions, Pending Deactivations, Recent Logins។
  2. បង្ហាញក្រាហ្វិក ឬបញ្ជីសកម្មភាពចុងក្រោយបង្អស់ក្នុងប្រព័ន្ធ។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"នេះជា **Admin Overview Dashboard** ដែលផ្តល់នូវទិដ្ឋភាពរួមនៃសុខភាពប្រព័ន្ធទាំងមូល។ Admin អាចមើលឃើញចំនួន User សរុប, ចំនួន Session ដែលកំពុងដំណើរការ, និងចំនួនសំណើសុំបិទគណនីដែលកំពុងរង់ចាំការពិនិត្យ (Pending Deactivations)។"*

---

### ៣.២. គ្រប់គ្រងអ្នកប្រើប្រាស់ (User Management & User Detail)
- **URL:** `http://localhost:9000/admin/users` និង `/admin/users/:id`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញបញ្ជី User ទាំងអស់ក្នុងប្រព័ន្ធ ជាមួយ Pagination និង Search/Filter។
  2. ចុចចូលមើល User ម្នាក់ (ឧទាហរណ៍ User ដែលបានស្នើសុំ Deactivate)។
  3. បង្ហាញ **Deactivation Proposal Banner** ពណ៌លឿងនៅផ្នែកខាងលើនៃទំព័រ User Detail!
  4. បង្ហាញប៊ូតុង Approve & Reject ផ្ទាល់នៅលើ User Detail។
  5. បង្ហាញសកម្មភាពរដ្ឋបាលផ្សេងទៀត៖ Enable/Disable, Lock/Unlock, Reset Password, Assign Roles/Permissions។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"នៅទំព័រ **User Management** Admin អាចគ្រប់គ្រង User គ្រប់រូបក្នុងប្រព័ន្ធបាន។ ពិសេសនៅពេលបើកមើល User Detail ប្រសិនបើ User នោះមានដាក់សំណើសុំបិទគណនី ប្រព័ន្ធនឹងបង្ហាញ Banner ជូនដំណឹងភ្លាមៗ អមដោយប៊ូតុង 'Approve & Deactivate' និង 'Reject Request' ដែលអនុញ្ញាតឱ្យ Admin ចាត់វិធានការបានភ្លាមៗ។"*

---

### ៣.៣. ទំព័រពិនិត្យសំណើសុំបិទគណនី (Deactivations Review Queue)
- **URL:** `http://localhost:9000/admin/deactivations`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. ចុចចូល Menu *"Deactivations / សំណើសុំបិទគណនី"* នៅលើ Sidebar។
  2. បង្ហាញបញ្ជីសំណើទាំងអស់ដែល User បានផ្ញើមក (Status: PENDING, PROCESSED, CANCELLED)។
  3. ចុចប៊ូតុង **"Review / ពិនិត្យ"** លើសំណើ PENDING មួយ។
  4. បង្ហាញ Modal ពិនិត្យ៖ បង្ហាញមូលហេតុ, ព័ត៌មានលម្អិត, និងប្រអប់វាយ Note របស់ Admin។
  5. សាកល្បងចុច **Approve** ឬ **Reject**៖
     - ប្រសិនបើ **Approve**៖ User នឹងត្រូវបាន Disable, Sessions ទាំងអស់ត្រូវបានកាត់ផ្តាច់ (Terminated), និង Status ប្តូរជា `PROCESSED`។
     - ប្រសិនបើ **Reject**៖ សំណើប្តូរជា `REJECTED` ហើយគណនី User នៅតែដំណើរការធម្មតា។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"នេះគឺជាទំព័រ **Deactivation Requests Review** ដែលបង្កើតឡើងដើម្បីឆ្លើយតបនឹងតម្រូវការ Admin Review។ រាល់សំណើទាំងអស់ត្រូវបានប្រមូលផ្តុំនៅទីនេះ។ នៅពេល Admin ពិនិត្យ និងចុច Approve ប្រព័ន្ធនឹងដំណើរការតាម `ApproveDeactivationRequestUseCase` ដោយបិទគណនី User (`enabled = false`), កាត់ផ្តាច់ Sessions ទាំងអស់របស់ User នោះចេញពីប្រព័ន្ធភ្លាមៗ និងកត់ត្រាទុកក្នុង Audit Log ថា Admin រូបណាជាអ្នកអនុម័ត និងកំណត់សម្គាល់អ្វីខ្លះ។"*

---

### ៣.៤. គ្រប់គ្រងតួនាទី និងសិទ្ធិ (Roles & Permissions)
- **URL:** `http://localhost:9000/admin/roles` និង `http://localhost:9000/admin/permissions`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញបញ្ជី Roles ដូចជា `ROLE_ADMIN`, `ROLE_USER`, `ROLE_STAFF`។
  2. បង្ហាញការផ្ដល់សិទ្ធិ (Permissions mapping) ទៅកាន់ Role នីមួយៗ (RBAC model)។
  3. បង្ហាញទំព័រ Permissions: សិទ្ធិកម្រិតលម្អិត (Granular Authorities) ដូចជា `user:read`, `user:write`, `role:read`, `client:read`, `audit:read`។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"ប្រព័ន្ធ IAM នេះអនុវត្តតាមស្តង់ដារ **Role-Based Access Control (RBAC)**។ តួនាទីនីមួយៗមានបណ្តុំសិទ្ធិ (Permissions) ជាក់លាក់។ Token JWT ដែលចេញឱ្យ Client មានផ្ទុកសិទ្ធិទាំងនេះក្នុង Claim `scope` ឬ `authorities` ដើម្បីឱ្យ Microservices ផ្សេងទៀតយកទៅផ្ទៀងផ្ទាត់បន្តបានយ៉ាងងាយស្រួល។"*

---

### ៣.៥. គ្រប់គ្រងអតិថិជន OAuth2 (OAuth2 Clients)
- **URL:** `http://localhost:9000/admin/clients`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញបញ្ជី Registered Clients (ដូចជា web-client, mobile-app, internal-service)។
  2. បង្ហាញ Client ID, Redirect URIs, Grant Types (authorization_code, refresh_token, client_credentials)។
  3. បង្ហាញប៊ូតុង Regenerate Secret និង Create Client ថ្មី។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"ទំព័រ **Clients** ប្រើសម្រាប់ចុះឈ្មោះ និងគ្រប់គ្រងកម្មវិធីខាងក្រៅ (OAuth2 Registered Clients)។ Admin អាចកំណត់ Redirect URIs, Scopes, Grant Types និងបង្កើត Secret ថ្មីបានដោយសុវត្ថិភាព។"*

---

### ៣.៦. តាមដានការចូលប្រព័ន្ធសរុប (Global Login Monitor)
- **URL:** `http://localhost:9000/admin/login-history`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញកំណត់ត្រា Login របស់អ្នកប្រើប្រាស់ទាំងអស់ក្នុងប្រព័ន្ធ។
  2. បង្ហាញ Filter តាមរយៈ Username, Status (Success/Failure), និងកាលបរិច្ឆេទ។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"ទំព័រ **Login Monitor** នេះផ្តល់លទ្ធភាពឱ្យ Security Officer ឬ Admin ត្រួតពិនិត្យការចូលប្រើប្រព័ន្ធរបស់អ្នកប្រើប្រាស់ទាំងអស់ ដើម្បីងាយស្រួលរកឃើញការវាយប្រហារ Brute-force ឬការចូលប្រើប្រាស់ពីទីតាំងមិនប្រក្រតី។"*

---

### ៣.៧. កំណត់ត្រាសវនកម្មប្រព័ន្ធ (Global Audit Logs)
- **URL:** `http://localhost:9000/admin/audit-logs`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញកំណត់ត្រាសកម្មភាពសំខាន់ៗក្នុងប្រព័ន្ធទាំងមូល៖ Action, Category, Actor (អ្នកធ្វើ), Target (គោលដៅ), IP Address, និងពេលវេលា។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"នេះជាទំព័រ **Audit Logs** សម្រាប់កិច្ចការ Compliance និង Security Tracking។ រាល់សកម្មភាពធំៗដូចជាការបង្កើត User, ការផ្លាស់ប្តូរសិទ្ធិ, ការ Approve/Reject សំណើបិទគណនី, និងការប្តូរការកំណត់ប្រព័ន្ធ សុទ្ធតែត្រូវបានកត់ទុកជាភស្តុតាងនៅទីនេះដែលមិនអាចកែប្រែបាន។"*

---

### ៣.៨. ការកំណត់ប្រព័ន្ធ (System Configuration)
- **URL:** `http://localhost:9000/admin/system`
- **សកម្មភាពត្រូវធ្វើ (Action):**
  1. បង្ហាញការកំណត់ Dynamic System Configurations (ឧ. Password complexity, Max login attempts, Session timeout, Self-registration toggle)។
- **🎙️ ពាក្យនិយាយពន្យល់គ្រូ៖**
  > *"ទំព័រចុងក្រោយគឺ **System Configuration** ដែលអនុញ្ញាតឱ្យ Admin កែប្រែគោលការណ៍សុវត្ថិភាពមួយចំនួនក្នុង Runtime បានដោយមិនបាច់ Restart Service ឡើយ។"*

---

## ផ្នែកទី ៤៖ គន្លឹះឆ្លើយសំណួរការពារមុខគ្រូ (Teacher Q&A Defense Tips)

ខាងក្រោមនេះជាសំណួរគន្លឹះដែលលោកគ្រូ/អ្នកគ្រូបច្ចេកទេសតែងតែសួរ និងចម្លើយគំរូដែលគួរឆ្លើយ៖

### សំណួរទី ១៖ ហេតុអ្វីបានជាជ្រើសរើសស្ថាបត្យកម្ម Hexagonal Architecture?
> **ចម្លើយ៖**  
> *"Hexagonal Architecture (Ports and Adapters) ជួយបំបែក Business Logic ឱ្យនៅឯករាជ្យទាំងស្រុងពី Framework និង Database។ ក្នុងគម្រោងយើង Domain Core និង Use Cases មិនអាស្រ័យលើ Spring Data ឬ Web Controller ឡើយ។ រាល់ការទាក់ទងខាងក្រៅត្រូវធ្វើតាម Ports (Interfaces) ហើយ Persistence ឬ REST Controller គ្រាន់តែជា Adapters ប៉ុណ្ណោះ។ វាជួយឱ្យយើងសរសេរ Unit Test បានលឿន ងាយស្រួល Refactor និងអាចប្តូរ Database ឬ Security Provider បានដោយមិនប៉ះពាល់ Core Logic ឡើយ។"*

---

### សំណួរទី ២៖ តើប្រព័ន្ធការពារ Brute-Force Password Login ដោយរបៀបណា?
> **ចម្លើយ៖**  
> *"យើងប្រើប្រាស់ `LoginAttemptListener` ដែលចាប់ `AuthenticationFailureBadCredentialsEvent`។ រាល់ពេល User វាយ Password ខុស យើងកត់ត្រាការបរាជ័យចូល `login_history` និងគណនាចំនួនដងនៃការបរាជ័យក្នុង `RecordLoginAttemptUseCase`។ ប្រសិនបើខុសលើសចំនួនកំណត់ (ឧទាហរណ៍ ៥ ដង) គណនីនឹងត្រូវ Lock ជាបណ្តោះអាសន្ន (`locked_until`) ឬ Lock រហូតទាល់តែ Admin ដោះសោឱ្យ។"*

---

### សំណួរទី ៣៖ តើ Session និង Token ដំណើរការយ៉ាងដូចម្តេចក្នុង Single Page Application (SPA)?
> **ចម្លើយ៖**  
> *"យើងអនុវត្តតាមស្តង់ដារ **OAuth 2.1 Authorization Code Flow ជាមួយ PKCE (Proof Key for Code Exchange)**។ Frontend React មិនរក្សាទុក Client Secret ឡើយ។ នៅពេល Login ជោគជ័យ Frontend ទទួលបាន Access Token (JWT) ដែលផ្ទុកនៅក្នុង Memory និងប្រើសម្រាប់ហៅ API។ ចំណែក Session របស់ Identity Provider ត្រូវបានគ្រប់គ្រងនៅលើ Spring Authorization Server តាមរយៈ Secure Session Cookie (`JSESSIONID`)។"*

---

### សំណួរទី ៤៖ តើ Deactivation Request Workflow ដំណើរការខុសពីការ Delete User ធម្មតាយ៉ាងដូចម្តេច?
> **ចម្លើយ៖**  
> *"ក្នុងប្រព័ន្ធ Enterprise ឬ Banking ការ Delete User ភ្លាមៗអាចធ្វើឱ្យបាត់បង់ទិន្នន័យពាក់ព័ន្ធ និងបាត់បង់ Audit Trail។ ដូច្នេះយើងអនុវត្ត **Proposal-Approval Pattern**៖
> 1. User បញ្ជូនសំណើសុំបិទគណនី (Deactivation Proposal) ជាមួយមូលហេតុច្បាស់លាស់។
> 2. សំណើចូលទៅកាន់ Queue របស់ Admin ដើម្បី Review។
> 3. នៅពេល Admin Approve ប្រព័ន្ធនឹង Disable User (`enabled = false`), Terminate Session ចោលទាំងអស់, និងកត់ត្រា Audit Log ទុកជាប្រវត្តិ ដោយមិនលុបទិន្នន័យចេញពី Database ឡើយ (Soft-disable)។"*

---

## 🏁 សេចក្តីសន្និដ្ឋានបញ្ចប់ការ Demo (Closing Statement)

> *"សរុបមក គម្រោង **IAM Service** នេះត្រូវបានសាងសង់ឡើងយ៉ាងរឹងមាំ ដោយរួមបញ្ចូលគ្នារវាងស្តង់ដារសុវត្ថិភាពអន្តរជាតិ (OAuth 2.1, OIDC, PKCE, RBAC), ស្ថាបត្យកម្មស្អាត (Hexagonal Architecture), និងបទពិសោធន៍អ្នកប្រើប្រាស់ងាយស្រួលតាមរយៈ Single Page Application ទំនើប។ 
>
> ខ្ញុំបាទ/នាងខ្ញុំសូមអរគុណយ៉ាងជ្រាលជ្រៅចំពោះការយកចិត្តទុកដាក់ស្តាប់របស់លោកគ្រូ/អ្នកគ្រូ។ សូមគោរពអញ្ជើញលោកគ្រូ/អ្នកគ្រូផ្តល់មតិរិះគន់ និងសួរសំណួរបន្ថែមបាទ/ចាស! សូមអរគុណ។"*
