# Cinema E-Booking System

A React/Vite frontend, Spring Boot REST API, and MySQL database for movie
browsing, authenticated admin movie/showtime/promotion management, and a
customer booking flow through a mock payment page, alongside registration,
profiles, favorites, and subscriber-only promotion emails.

## Requirements

- Java 17
- MySQL
- Node.js 22.12 or newer (Vite requirement) and npm

## Start locally

### 1. Create the database

Back up any existing database before changing its schema. To create a fresh
development database and seed movies, run this once only on a new/empty local
database:

```sh
mysql -u root -p < database/cinema_db.sql
```

For an existing database, back it up and run
[`database/migrate_sprint3.sql`](./database/migrate_sprint3.sql) once to add
showroom-specific seat layouts and demo showtimes. If that database also has
legacy registration columns, review and run
[`database/migrate_registration_columns.sql`](./database/migrate_registration_columns.sql)
as described in that migration's instructions.

### 2. Configure and run the backend

The backend uses `cinema_db_schema` by default. Use
`backend/src/main/resources/application-example.properties` as the template for
your local, Git-ignored `backend/src/main/resources/application.properties`.
If the local file does not exist yet, copy the template (do not overwrite an
existing local configuration):

```sh
cp backend/src/main/resources/application-example.properties backend/src/main/resources/application.properties
```

Set your MySQL connection details there and configure SMTP for email
verification and promotion email. The template supports `SMTP_HOST`,
`SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_AUTH`, `SMTP_STARTTLS`,
`APP_MAIL_FROM`, `APP_FRONTEND_URL`, and `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`.
For local email testing, install and start Mailpit in another terminal:

```sh
brew install mailpit
mailpit --listen 127.0.0.1:8025 --smtp 127.0.0.1:1025
```

Open `http://127.0.0.1:8025` to view messages. The example configuration uses
Mailpit by default; Gmail setup is documented in the
[backend guide](./backend/README.md).
Never commit database credentials, SMTP passwords, encryption keys, or database
dumps.

Payment-card encryption requires a stable, private AES-256 key. For local
development, store the key in the Git-ignored
`backend/src/main/resources/application.properties` or provide it through the
backend environment. This machine's key is not included in Git; every developer
must configure and securely retain their own key. Generate a key once and save
it in a password manager:

```sh
openssl rand -base64 32
```

Then start MySQL and the backend. In Terminal 1, from the repository root:

```sh
cd backend
read -rs "PAYMENT_CARD_ENCRYPTION_KEY?Saved payment-card key (input hidden): "
printf '\n'
export PAYMENT_CARD_ENCRYPTION_KEY
./mvnw spring-boot:run
```

Keep the same key across restarts. Losing it makes encrypted card numbers
unrecoverable. The backend runs at `http://localhost:8080`. Keep this terminal
running. Registration, verification, password reset, and promotion-email
delivery require a working SMTP server.

### 3. Run the frontend

In Terminal 2, from the repository root:

```sh
cd frontend
npm install
npm run dev
```

Open the URL Vite prints (normally `http://localhost:5173`). Keep this terminal
running. The Vite dev server proxies `/api` requests to the backend so browser
session cookies work.

### 4. Seed the local demo admin and showtimes (opt-in)

The demo seeder is disabled during normal starts. To create/update the local
admin and ensure every movie in the catalog has three future showtimes with
seat maps:

1. Stop the normal backend with **Control+C** in Terminal 1.
2. In that same terminal, run:

   ```sh
   read -rs "DEMO_SEED_PASSWORD?Set the demo admin password (input hidden): "
   printf '\n'
   export DEMO_SEED_PASSWORD
   export DEMO_SEED_ENABLED=true
   ./mvnw spring-boot:run
   ```

3. Wait for `Demo admin account is ready.` The seed operation uses the
   configured database, rooms, and seat layouts. It does not seed customer
   accounts; create customers through website registration.
4. Stop the backend with **Control+C** and disable the seeder:

   ```sh
   unset DEMO_SEED_PASSWORD DEMO_SEED_ENABLED
   ```

5. Start the backend normally again:

   ```sh
   ./mvnw spring-boot:run
   ```

The admin email is `kndllhaynes@gmail.com`; use the password entered at the
hidden prompt. Enabling the seeder resets that admin password on every run.
Use it only with a local/demo database. The sample showtimes are scheduled
across the three demo auditoriums; newly added movies also receive demo
showtimes the next time the opt-in seeder runs.

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
changes as a production admin-management process. Admins can add movies,
schedule future showtimes, and create promotions from the Admin Portal.
User management remains out of scope.

## Sprint 3 movie, showtime, and booking flow

The Admin Portal provides validated movie creation and showtime scheduling.
Showtimes must be in the future, use a configured showroom, and cannot duplicate
the same showroom/date/time. The fresh database script and the Sprint 3
migration provide three demo auditoriums with 6-by-8 seat layouts and sample
screenings.

Customers select a movie and database-backed showtime, choose ticket quantities
and available seats, then continue to checkout. Checkout requires a signed-in
customer; the current booking selection remains in the browser during login.
Selecting a seat holds it for the browser’s server session. Other sessions see
it as unavailable. Deselecting a seat, replacing the selection, signing out,
or session expiration releases the corresponding hold.
The order summary shows movie, showtime, seat labels, ticket types/prices, and
the total before tax. Customers can confirm or change the email for this
checkout before viewing the mock payment-processing page. No payment is
processed and no final booking confirmation is issued.

### Promotions and subscribed-customer email

In the Admin Portal, open **Manage Promotions** and submit a promotion name,
discount percentage (greater than 0 and at most 100), start date, and end date.
The server validates all fields and date ordering, saves the promotion, and
sends a notification only to active customer accounts that opted in to
promotion email during registration. The result reports how many messages were
sent and how many failed. Configure working SMTP before demonstrating this
feature. Unsubscribed and inactive accounts are not recipients.

## Deliverable 6 requirements review

| Requirement | Implementation / demo check |
|---|---|
| Admin home menu and navigation | Manage Movies, Manage Promotions, Manage Users, and Manage Showtimes are available. User management is informational because it is explicitly not required. |
| Add movie and validation | Admin form validates required title, genre, status, and description; the admin API validates and persists the movie. Verify it appears in the customer catalog. |
| Schedule showtime and prevent conflicts | Admin selects a movie, future date/time, and showroom. Server rejects a duplicate showroom/date/time; each configured showroom must have seats. |
| Three showrooms and seed data | Fresh SQL setup provides three 6-by-8 demo auditoriums. Opt-in backend seeding gives every catalog movie at least three future showtimes with seat maps. |
| Promotion bonus | Required name, 0.01–100% discount, and date range are validated and saved. Email is sent only to active users whose promotion preference is enabled; the result reports send failures. |
| Showtime visibility and booking | Customer movie pages load future showtimes from MySQL. Customer selects adult/child/senior quantities and an equal number of available seats. |
| Seat selection and checkout/login | Guests can select seats. Proceeding to checkout requires customer login and preserves the selection through login. The server verifies availability and ticket count, then marks those seats unavailable for that showtime. |
| Order summary and email | Summary includes movie, showtime, seats, ticket counts/types, per-ticket prices, and pre-tax total. It starts with the account email and lets the customer confirm or change it before continuing. |
| Payment page | Displays a payment-processing mockup only. It does not collect card data, process payments, or finalize bookings. |
| Architecture, validation, and test readiness | React UI, REST controllers, services, repositories, and MySQL follow a layered architecture. Validation is client- and server-side; backend unit tests cover scheduling, reservations, and seeding. Use the invalid showtime conflict and seat-already-taken paths in the demo. |

The single-server demo tracks temporary holds in memory. A server restart clears
those holds. MySQL remains the source of truth for booked tickets and the seat map.

## Database and backend references

- [Complete setup, account/email workflows, API overview, and feature guide](./PROJECT-GUIDE.md)
- [Pre-seeded demo tutorial](./DEMO-TUTORIAL.md)
- [Backend endpoints, SMTP, encryption-key handling, and security details](./backend/README.md)
- [Database schema and setup](./database/README.md)

The API uses `/api/auth` for registration, verification, login, logout, current
session, and password reset; `/api/profile` for protected profile, card, and
password updates; `/api/favorites` for protected favorites; `/api/admin/session`
to verify an administrator role; `/api/movies/admin` for admin-only movie
creation; `/api/showrooms` and `/api/showtimes` for showtime/seat data;
`POST /api/showtimes/admin` for admin-only scheduling;
`PUT /api/showtimes/{showId}/selection` for session holds and authenticated
`POST /api/showtimes/{showId}/reserve` for checkout seat
reservations; and `POST /api/promotions/admin` for admin-only promotion
creation and subscriber email delivery.

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
- User management is not required for this sprint.
- Promotion emails require recipients to be active and have opted in to
  promotions; delivery failures are reported in the Admin Portal.
- The payment page is a mockup: no payment is processed and no booking is
  finalized. Temporary seat holds end when replaced or when the session ends.
