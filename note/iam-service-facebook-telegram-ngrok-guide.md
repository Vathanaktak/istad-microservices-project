# IAM Service: Facebook & Telegram Login Integration with ngrok Guide

This document records everything configured for **Facebook OAuth2** and **Telegram Login Widget** in [`iam-service`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service), along with a complete step-by-step guide on how to run and test the application publicly using **ngrok**.

---

## 1. Summary of What Has Been Done

### A. Facebook OAuth2 Integration
1. **Credentials Setup**:
   - Created a Meta Developer App at [developers.facebook.com](https://developers.facebook.com/).
   - Retrieved **App ID** (`FACEBOOK_CLIENT_ID`) and **App Secret** (`FACEBOOK_CLIENT_SECRET`).
   - Stored credentials in [`microservices/iam-service/.env`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/.env).

2. **Redirect URI Configuration**:
   - Set Valid OAuth Redirect URI in Facebook Login settings:
     ```text
     http://localhost:9000/login/oauth2/code/facebook
     ```
   - **Key Fix (Disappearing URI)**: The redirect URI input in Meta Console is a tag/chip field. You must press <kbd>Enter</kbd> to convert the URL into a badge/pill before clicking **Save changes**, and ensure the **Website** platform is added under **App settings → Basic** with Site URL `http://localhost:9000/`.

3. **Resolved "This app needs at least one supported permission"**:
   - Added `email` and `public_profile` under **Use cases → Authentication and account creation → Customize → Permissions** (or **App Review → Permissions and Features**).

4. **Resolved "Invalid Privacy Policy URL" / Live Mode requirement**:
   - Clarified that **Advanced Access / Live Mode is NOT required for local testing**.
   - Kept the app in **Development Mode**, where **Standard Access** is automatically granted for the App Administrator and accounts listed under **App roles → Roles**.

---

### B. Telegram Login Widget Integration
1. **Understanding Telegram Authentication**:
   - Telegram does not use standard OAuth2 (`client_id` / `client_secret`).
   - It uses the official **Telegram Login Widget** powered by:
     - `TELEGRAM_BOT_USERNAME` (serves as the client ID)
     - `TELEGRAM_BOT_TOKEN` (serves as the secret key for cryptographic HMAC-SHA256 signature verification)

2. **Bot Creation with `@BotFather`**:
   - Created a new Telegram bot via `/newbot`.
   - Retrieved the **HTTP API Bot Token** and **Bot Username**.

3. **Domain Binding (`/setdomain`)**:
   - Telegram requires every bot using the login widget to have an authorized domain.
   - **Key Fix ("Invalid bot selected")**: In `@BotFather`, after sending `/setdomain`, you must click your bot's button first before pasting the domain name.

4. **Public Tunnel Requirement**:
   - Telegram rejects `localhost` or raw IP addresses in `/setdomain`.
   - Resolved by tunneling port `9000` through **ngrok** to provide a public HTTPS endpoint (e.g. `epidermis-ashy-shush.ngrok-free.dev`).

---

## 2. Environment Variables Reference

Ensure [`microservices/iam-service/.env`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/.env) has the following configured:

```env
# Database
DB_URL=jdbc:postgresql://localhost:9543/db_iam
DB_USERNAME=iteuser
DB_PASSWORD=qwerqwer
IAM_DCR_CLIENT_SECRET=change-me-too

# OAuth2: Google
GOOGLE_CLIENT_ID=your_google_client_id_here
GOOGLE_CLIENT_SECRET=your_google_client_secret_here

# OAuth2: GitHub
GITHUB_CLIENT_ID=your_github_client_id_here
GITHUB_CLIENT_SECRET=your_github_client_secret_here

# OAuth2: Facebook
FACEBOOK_CLIENT_ID=your_facebook_client_id_here
FACEBOOK_CLIENT_SECRET=your_facebook_client_secret_here

# Telegram Login Widget
TELEGRAM_BOT_TOKEN=your_bot_token_here
TELEGRAM_BOT_USERNAME=your_bot_username_without_at
```

---

## 3. How to Run the Application with ngrok

Follow this exact workflow to run and test the complete authentication suite.

### Step 1: Start the PostgreSQL Database
Make sure the database container for `iam-service` is running on port `9543`:
```powershell
cd D:\ISTAD\microservices\istad-ite-project\deployment\postgres
docker compose up -d
```

---

### Step 2: Start the ngrok Tunnel
1. If ngrok is not installed, install it using:
   ```powershell
   winget install ngrok/ngrok
   ```
2. Configure your ngrok authtoken (one-time setup):
   ```powershell
   ngrok config add-authtoken <YOUR_NGROK_AUTHTOKEN>
   ```
3. Expose port `9000` (where `iam-service` runs):
   ```powershell
   ngrok http 9000
   ```
4. Look at the terminal output to find your forwarding HTTPS URL, for example:
   ```text
   Forwarding   https://epidermis-ashy-shush.ngrok-free.dev -> http://localhost:9000
   ```
   > ⚠️ **Note**: Leave this terminal window open while testing.

---

### Step 3: Link the ngrok Domain in Telegram (`@BotFather`)
Whenever your ngrok domain changes:
1. Open Telegram and message **`@BotFather`**.
2. Send:
   ```text
   /setdomain
   ```
3. Click your bot's button from the list.
4. Send your active ngrok domain (without `https://` and without slashes):
   ```text
   epidermis-ashy-shush.ngrok-free.dev
   ```
5. `@BotFather` will reply:
   > *"Success! Domain was updated to epidermis-ashy-shush.ngrok-free.dev"*

---

### Step 4: Run `iam-service`
Run [`IamApplicationService.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-main/src/main/java/kh/edu/istad/platform/iam/IamApplicationService.java):

* **From IntelliJ IDEA**:
  Open `IamApplicationService.java` and click the green **Run** button.
* **Or from Terminal**:
  ```powershell
  cd D:\ISTAD\microservices\istad-ite-project
  .\gradlew :microservices:iam-service:iam-service-main:bootRun
  ```

---

### Step 5: Test the Login Flows

#### 1. Testing Telegram Login
> ⚠️ **Important**: Telegram checks `window.location.host`. You **must** open the ngrok HTTPS URL in your browser, not `localhost`.

1. Open your browser to:
   ```text
   https://epidermis-ashy-shush.ngrok-free.dev/login
   ```
   *(If prompted by the ngrok interstitial warning, click "Visit Site")*.
2. You will see the blue **"Log in with Telegram"** button widget.
3. Click the button:
   - Enter your phone number or confirm the popup prompt.
   - Confirm the authorization request inside your Telegram app.
4. Telegram will redirect back to `/login/telegram`.
5. [`TelegramLoginFilter.java`](file:///D:/ISTAD/microservices/istad-ite-project/microservices/iam-service/iam-service-infrastructure/src/main/java/kh/edu/istad/platform/iam/infrastructure/federation/TelegramLoginFilter.java) validates the signature, provisions your user, and completes login.

#### 2. Testing Facebook Login
1. Open `http://localhost:9000/login` (or the ngrok URL).
2. Click **"Continue with Facebook"**.
3. Log in with the Facebook account that created the Meta App (or any tester account added under **App roles → Roles**).
4. Approve the permissions (`public_profile`, `email`).
5. Spring Security completes the OAuth2 handshake and redirects you into the application.

---

## 4. Troubleshooting & FAQ

| Problem | Cause | Solution |
| :--- | :--- | :--- |
| **Telegram button does not show up** | `TELEGRAM_BOT_USERNAME` is missing or empty in `.env`. | Add `TELEGRAM_BOT_USERNAME=bot_username` in `.env` and restart the application. |
| **Telegram popup shows "Bot domain invalid"** | Domain in browser does not match `/setdomain` in `@BotFather`. | Run `/setdomain` with `@BotFather` and enter the exact domain you are browsing on. |
| **Facebook: "This app needs at least one supported permission"** | `email` or `public_profile` not added in Meta dashboard. | Go to Meta App dashboard → **Use cases** → **Customize** on Facebook Login → Add `email` and `public_profile`. |
| **Facebook: "App not active"** | App is in Development mode and user is not an admin/tester. | Log in using the Facebook developer account, or add the test account under **App roles → Roles**. |
| **Facebook Redirect URI disappears on save** | Didn't press <kbd>Enter</kbd> before saving in Meta dashboard. | Paste `http://localhost:9000/login/oauth2/code/facebook`, press <kbd>Enter</kbd> so it turns into a tag/pill, then click **Save changes**. |
