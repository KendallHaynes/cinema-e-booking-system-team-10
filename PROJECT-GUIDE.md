# Cinema E-Booking System: Setup and Feature Guide

This guide covers local setup, running the application, account and email
workflows, profile features, demo preparation, and the changes included in the
authentication/profile feature branch.

## What is in the project

- `frontend/`: React application built with Vite.
- `backend/`: Java 17 and Spring Boot REST API.
- `database/cinema_db.sql`: MySQL schema and movie sample data.
- `DEMO-TUTORIAL.md`: step-by-step preparation and use of the demo accounts.

The current admin portal is an authenticated navigation prototype. Its menu
sections do not yet provide working movie, user, promotion, or showtime
management.

## Requirements

- Java 17
- MySQL
- Node.js 22.12 or newer and npm
- macOS/Linux shell examples below use `zsh`/Bash syntax

## Initial local setup

### 1. Create the database

Back up any database you need to keep. From the repository root, import the
schema and movie sample data:

```sh
mysql -u root -p < database/cinema_db.sql
```

The SQL script creates `cinema_db_schema`. Ensure the JDBC URL in your local
backend configuration points to the same database. If you already have a
database, inspect its schema before running setup or migration SQL.

### 2. Configure the backend

Copy `backend/src/main/resources/application-example.properties` to
`backend/src/main/resources/application.properties`. The latter is local
configuration and must not be committed. Set the MySQL username/password and
matching database name in that file. Keep SMTP credentials and encryption keys
out of source control.

Payment-card encryption requires a stable, private base64-encoded AES-256 key.
Generate one once:

```sh
openssl rand -base64 32
```

Save it in a password manager and configure it as
`app.card-encryption-key` in the ignored local properties file or use
`PAYMENT_CARD_ENCRYPTION_KEY` in the backend process environment. Do not lose
or replace the key after encrypted card records are stored: the original card
numbers cannot be recovered without it.

### 3. Configure email

The application sends email through SMTP. For local development, Mailpit can
receive test email without delivering it to external mailboxes:

```sh
brew install mailpit
mailpit --listen 127.0.0.1:8025 --smtp 127.0.0.1:1025
```

Open `http://127.0.0.1:8025` to view messages. Keep Mailpit running in its own
terminal while testing. The backend's local SMTP defaults should point to
`localhost:1025`; if needed, configure `SMTP_HOST`, `SMTP_PORT`,
`SMTP_AUTH=false`, and `SMTP_STARTTLS=false`.

To send through Gmail instead, enable 2-Step Verification and create an App
Password for the sender account. Do not use the regular Google password. In
the terminal used to start the backend, enter the App Password without
printing or saving it in shell history:

```sh
read -rs "SMTP_PASSWORD?Gmail App Password (input hidden): "
printf '\n'
export SMTP_PASSWORD
export SMTP_HOST=smtp.gmail.com SMTP_PORT=587
export SMTP_USERNAME=your-sender@gmail.com APP_MAIL_FROM=your-sender@gmail.com
export SMTP_AUTH=true SMTP_STARTTLS=true
```

The address a customer enters during registration is the recipient; Gmail is
the sender account. Stop the backend before changing SMTP settings, then start
it in the terminal where those settings were exported.

### 4. Start the backend

From the repository root:

```sh
cd backend
./mvnw spring-boot:run
```

Keep the terminal open. The API normally listens on `http://localhost:8080`.
For example, `http://localhost:8080/api/movies` should return the movie list.
Use the Maven wrapper; launching the main class with only `target/classes`
omits Spring dependencies.

### 5. Start the frontend

In another terminal, from the repository root:

```sh
cd frontend
npm install
npm run dev
```

Open the Vite URL, normally `http://localhost:5173`. The development server
proxies `/api` requests to the backend so browser session cookies work.

## Account and email workflows

### Registration and verification

1. Select **Create an account** and provide first name, last name, email,
   password, and confirmation.
2. The password must be 10–72 characters and include uppercase, lowercase,
   numeric, and symbol characters.
3. Email is normalized for matching. A duplicate address receives an
   “An account with this email already exists” response.
4. The backend stores the password as a BCrypt hash and creates the account
   with an inactive/unverified status.
5. The backend sends **“Confirm your Cinema E-Booking account”** to the
   supplied address. Open the message in the recipient inbox or Mailpit and
   follow its verification link within 24 hours.
6. Successful verification activates the account and consumes the one-time
   token. The database stores a hash of the token, not the original token.

An unverified customer who signs in sees:

> Account is not verified. Please check your email to verify your account.

### Login and logout

Sign in with the registered email/password after verification. A successful
login creates a server-side session with an HTTP-only cookie. Refreshing the
page restores the session from the backend. Log out to invalidate it. Admin
access is based on the account role returned by the backend, not a role value
held only in the browser.

### Forgot and reset password

Choose **Forgot my password** and enter the account email. If it matches an
account, the backend sends **“Reset your Cinema E-Booking password”** with a
single-use link valid for 30 minutes. The web response is intentionally similar
whether or not an account exists. Enter and confirm a new password that meets
the same password requirements. Reset tokens are stored as hashes and removed
after successful use.

### Profile, address, cards, and favorites

- **My Profile:** edit first and last name; the email address is read-only.
- **Address:** save one address used for billing. A billing address is required
  before a payment card can be saved.
- **Cards:** save up to three cards. Card numbers are encrypted at rest using
  AES-256-GCM; the UI presents the brand and last four digits. The app does not
  request or save CVV. Do not enter real card numbers in this demo.
- **Password change:** enter the current password and a valid new password.
- **Profile notifications:** a profile information change sends
  **“Your Cinema E-Booking profile was updated”** to the account email. The
  notification does not include profile or payment details.
- **Favorites:** select a movie's heart to save/remove it. Signed-in favorites
  persist in MySQL; guest favorites remain in that browser.

### Viewing the application emails

With Mailpit running, open `http://127.0.0.1:8025`. Messages addressed to a
test email appear there. Use this to test registration verification,
password-reset links, and profile-change notifications without sending them to
real inboxes. When using Gmail SMTP, messages are delivered by the configured
sender to the customer's address instead.

## Prepare the pre-seeded demo

The demo data seeder is disabled during normal starts. Follow
[`DEMO-TUTORIAL.md`](./DEMO-TUTORIAL.md) to enable it once before the demo. It
creates or updates these accounts:

| Account | Email | Preloaded state |
|---|---|---|
| Kendall Admin | `kndllhaynes@gmail.com` | Admin role |
| Kendall Favorite | `kch55499@uga.edu` | Active customer with a saved movie favorite |
| Kendall Card | `kendallhaynes7730@gmail.com` | Active customer with an address and up to three saved cards |

Enter the shared demo password at the hidden prompt; it is deliberately not
written in this guide. New seeded cards are fake test values, encrypted with
the configured key. The seeder preserves existing addresses and cards, avoids
duplicating the favorite, and will not add more than three cards. The resulting
database records remain after stopping the seeded backend and starting it
normally. Disabling the seeder prevents future seed runs; it does not delete
existing records.

## Admin role for a separately registered account

For a local demo only, a trusted developer can promote an already registered
account using MySQL:

```sql
USE cinema_db_schema;

UPDATE users
SET userType = 'ADMIN',
    customerStatus = NULL,
    promotions = NULL
WHERE email = 'admin@example.com';
```

Check that one row was affected. Sign out and sign in again to refresh the
session role. To demote the account:

```sql
UPDATE users
SET userType = 'CUSTOMER',
    customerStatus = 'ACTIVE',
    promotions = FALSE
WHERE email = 'admin@example.com';
```

Do not expose demo accounts or manual role updates on a public deployment.

## API overview

| Endpoint | Purpose |
|---|---|
| `POST /api/auth/register` | Register a customer |
| `POST /api/auth/verify-email?token=...` | Verify a registration |
| `POST /api/auth/login` | Sign in |
| `POST /api/auth/logout` | End the session |
| `GET /api/auth/me` | Read the current session |
| `POST /api/auth/forgot-password` | Request a reset email |
| `POST /api/auth/reset-password` | Set a password with a reset token |
| `GET /api/profile` / `PUT /api/profile` | Read/update a signed-in profile |
| `POST /api/profile/cards` / `DELETE /api/profile/cards/{id}` | Add/remove a saved card |
| `POST /api/profile/password` | Change password |
| `GET /api/favorites` / `POST /api/favorites/{movieId}` / `DELETE /api/favorites/{movieId}` | List/add/remove favorites |
| `GET /api/admin/session` | Check whether the session belongs to an admin |
| `/api/movies` | Read/search/filter the movie catalog |

Protected profile/favorites endpoints require the signed-in session.

## Tests and checks

Backend tests:

```sh
cd backend
./mvnw test
```

Frontend lint and production build:

```sh
cd frontend
npm run lint
npm run build
```

## Security and project scope

- Never commit `application.properties`, SMTP credentials, encryption keys,
  database dumps, or the shared demo password.
- Use only a local/demo database for seeded demo accounts.
- Never use actual payment-card information. This prototype is not a payment
  processor or PCI-compliant card vault.
- Use HTTPS and secure cookies for deployment, and keep SMTP and encryption
  secrets in a managed secret store.
- The admin page is a navigation prototype; management operations and booking
  /payment processing are not implemented as complete backend features.
