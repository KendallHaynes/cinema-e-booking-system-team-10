# Cinema E-Booking System

A React/Vite frontend, Spring Boot REST API, and MySQL database for browsing
movies and demonstrating customer registration, authentication, account
profiles, favorites, and an admin landing-page prototype.

## Requirements

- Java 17
- MySQL
- Node.js 22.12 or newer (Vite requirement) and npm

## Start locally

### 1. Create the database

Back up any existing database before changing its schema. To create a fresh
development database and seed movies:

```sh
mysql -u root -p < database/cinema_db.sql
```

For an existing database with legacy registration columns, review
[`database/migrate_registration_columns.sql`](./database/migrate_registration_columns.sql)
and run it once after backing up the database.

### 2. Configure and run the backend

The backend uses `cinema_db_schema` by default. Configure database connection
and SMTP settings in a local `backend/src/main/resources/application.properties`
file or through environment variables. Do not commit credentials, SMTP app
passwords, encryption keys, or database dumps.

Payment-card encryption requires a stable, private AES-256 key. For local
development, store the key in the Git-ignored
`backend/src/main/resources/application.properties` or provide it through the
backend environment. This machine's key is not included in Git; every developer
must configure and securely retain their own key. Generate a key once and save
it in a password manager:

```sh
openssl rand -base64 32
```

Then either set the saved value in the backend process environment:

```sh
cd backend
read -rs "PAYMENT_CARD_ENCRYPTION_KEY?Saved payment-card key (input hidden): "
printf '\n'
export PAYMENT_CARD_ENCRYPTION_KEY
./mvnw spring-boot:run
```

Keep the same key across restarts. Losing it makes encrypted card numbers
unrecoverable. The backend runs at `http://localhost:8080`. Email verification,
password reset, and profile-change notifications need a working SMTP server.
See the [backend guide](./backend/README.md) for Gmail and local Mailpit setup.

### 3. Run the frontend

In another terminal:

```sh
cd frontend
npm install
npm run dev
```

Open the URL Vite prints (normally `http://localhost:5173`). The Vite dev
server proxies `/api` requests to the backend so browser session cookies work.

## Account and profile features

- **Register:** create an account with a unique email and a 10–72 character
  password containing uppercase, lowercase, a number, and a symbol. Confirm the
  email using the link delivered by SMTP before signing in.
- **Login/logout:** sign-in creates an HTTP-only server session. Use **Log out**
  to invalidate the session. A page refresh restores the session from the
  server; users are not authenticated based only on browser-provided role data.
- **Forgot password:** request a reset link from the sign-in page. The link is
  single-use and expires after 30 minutes. The response does not reveal whether
  the address belongs to an account.
- **Edit profile:** choose **My Profile** to edit the first and last name and
  save one mailing/billing address. The email is read-only. A personal
  information change triggers a security notification email.
- **Change password:** provide the current password and a valid new password.
  Passwords are stored as BCrypt hashes.
- **Payment cards:** a user may save up to three cards. Card numbers are
  encrypted with AES-256-GCM and the UI shows only brand/last four digits. The
  app never asks for or stores a CVV. The profile requires an address before a
  card can be saved.
- **Favorites:** select the heart on a movie to save/remove it. Signed-in
  favorites persist in MySQL across sessions; guest favorites remain in that
  browser.

## Granting an account admin access

For a demo, first register and verify the account normally. In MySQL Workbench
or another trusted database client connected to `cinema_db_schema`, run the
following statement with the exact registered email:

```sql
USE cinema_db_schema;

UPDATE users
SET userType = 'ADMIN',
    customerStatus = NULL,
    promotions = NULL
WHERE email = 'admin@example.com';
```

Check the result. **1 row affected** means the account was promoted; **0 rows
affected** means the email did not match a user. The admin signs in with the
same email and password already registered. Sign out first if the browser has
an existing session, then sign in again; the frontend checks the current
database role and opens the Admin Portal. The menu displays **Manage Movies**,
**Promotions**, **Users**, and **Showtimes**.

To demote that account back to a customer:

```sql
UPDATE users
SET userType = 'CUSTOMER',
    customerStatus = 'ACTIVE',
    promotions = FALSE
WHERE email = 'admin@example.com';
```

Only trusted developers should run these statements. Do not use manual SQL role
changes as a production admin-management process. The admin portal is a
navigation prototype; the listed management sections do not yet implement
admin operations.

## Preparing the demo accounts

The backend includes an opt-in demo seeder. It creates or updates these local
accounts before a demo:

- Admin: `kndllhaynes@gmail.com` (`Kendall Admin`)
- Verified favorite user: `kch55499@uga.edu` (`Kendall Favorite`), with a movie
  already saved to favorites
- Verified card user: `kendallhaynes7730@gmail.com` (`Kendall Card`), with an
  address and three saved cards (missing cards are filled with encrypted,
  demo-only test numbers)

The same password is set for all three accounts using the
`DEMO_SEED_PASSWORD` environment variable. The seeder is disabled by default.
Run the backend once with demo seeding enabled before the presentation; it is
idempotent and can be run again without duplicating favorites or adding cards
past the three-card limit. Existing matching accounts are updated to the demo
names, roles, active status, and password. Existing addresses and saved cards
are preserved; fake test cards are added until the account has three cards.

On macOS, enter the password without placing it in shell history:

```sh
cd backend
read -rs "DEMO_SEED_PASSWORD?Demo account password (input hidden): "
printf '\n'
export DEMO_SEED_PASSWORD
export DEMO_SEED_ENABLED=true
./mvnw spring-boot:run
```

Wait for the backend to report that demo data is ready. Stop it with
`Control+C`, then unset the temporary environment variables:

```sh
unset DEMO_SEED_PASSWORD DEMO_SEED_ENABLED
```

Start the backend normally afterward. Never use real payment-card numbers in
the demo; the seeded cards are test values and are encrypted using the local
card-encryption key.

## Database and backend references

- [Complete setup, account/email workflows, API overview, and feature guide](./PROJECT-GUIDE.md)
- [Pre-seeded demo tutorial](./DEMO-TUTORIAL.md)
- [Backend endpoints, SMTP, encryption-key handling, and security details](./backend/README.md)
- [Database schema and setup](./database/README.md)

The API uses `/api/auth` for registration, verification, login, logout, current
session, and password reset; `/api/profile` for protected profile, card, and
password updates; `/api/favorites` for protected favorites; `/api/admin/session`
to verify an administrator role; and `/api/movies` for the movie catalog.

## Verification

Run backend tests:

```sh
cd backend
./mvnw test
```

Run frontend lint and production build:

```sh
cd frontend
npm run lint
npm run build
```

## Security and scope notes

- Use HTTPS, secure cookie settings, private environment secrets, and managed
  key storage outside local development.
- Do not use real payment cards in this demo. Application-level encryption
  does not make the project a payment processor or PCI-compliant card vault.
- The admin page is a prototype, not a complete administration system.
- Booking, payment processing, promotions, and working admin-management tools
  remain outside the current implemented scope.
