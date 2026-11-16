# Cinema E-Booking System Demo Tutorial

This guide prepares the local database before the presentation so the demo
admin account is available when the demo begins.
The demo seeder is opt-in and does not run during a normal backend start.

## Demo accounts

| Account | Email | Demo data |
|---|---|---|
| Admin | `kndllhaynes@gmail.com` | Admin role |

The admin password is entered locally at a hidden prompt. It is not written in
this file or stored in source control. Customer accounts are not seeded; create
them through the registration page.

## Prepare demo data

Run this before the presentation. It temporarily stops the backend, starts it
with seeding enabled, waits for the seed to complete, then starts it normally.

1. Stop the currently running backend with **Ctrl+C** in its terminal.
2. From the repository root, enter:

   ```sh
   cd backend
   read -rs "DEMO_SEED_PASSWORD?Demo account password (input hidden): "
   printf '\n'
   export DEMO_SEED_PASSWORD
   export DEMO_SEED_ENABLED=true
   ./mvnw spring-boot:run
   ```

3. Wait for this message in the backend output:

   ```text
   Demo admin account is ready.
   ```

   If startup reports an error instead, do not continue to the demo. Resolve
   the error and run the seeding command again.

4. Stop the backend with **Ctrl+C**, then clear the temporary seeding settings:

   ```sh
   unset DEMO_SEED_PASSWORD DEMO_SEED_ENABLED
   ```

5. Start the backend normally:

   ```sh
   ./mvnw spring-boot:run
   ```

6. Start the frontend if it is not already running. In another terminal:

   ```sh
   cd frontend
   npm run dev
   ```

   Open the Vite URL, normally `http://localhost:5173`.

## What the seeder does

- Creates the admin account if it does not exist.
- Updates the matching admin account's name, role, and password if it already
  exists. Passwords are stored as BCrypt hashes.
- Ensures every movie in the catalog has three future showtimes with seat
  maps, using the configured demo auditoriums.
- Does not seed customer accounts, favorites, addresses, or payment cards.

Because seeding updates the admin password each time it runs, keep
`DEMO_SEED_ENABLED` enabled only for the one-time setup. Never enable this
seeder against a public or production database.

## Sign in during the demo

1. Open the frontend and choose **Login**.
2. Sign in with the admin email and the password you entered during seeding.
3. Register a customer account through the website if you also want to
   demonstrate the customer experience.

## Demonstrate Sprint 3

1. Sign in with the admin account and open **Manage Movies**. Add a movie with
   its required title, genre, description, and status; verify it appears in the
   catalog.
2. Open **Manage Showtimes**, choose a movie, showroom, and future date/time,
   then schedule it. The sample data includes three auditoriums; attempt to
   schedule the same showroom at the same time to demonstrate conflict
   validation.
3. Sign in as a customer, open a movie with an upcoming screening, and choose a
   showtime. Change adult/child/senior quantities, select the matching number
   of available seats, and verify booked seats are disabled.
4. Proceed to checkout. If signed out, log in and confirm the selection is
   still present. Review the order summary, confirm or change the email, and
   continue to the mock payment page.
5. To demonstrate the optional promotion bonus, register and verify a
   customer account with **Email me about promotions** checked, configure a
   working SMTP server, and create a promotion in **Manage Promotions**.
   Confirm the admin sees the recipient/failure counts and that only active,
   subscribed accounts receive the message.

Proceeding to checkout marks the selected seats unavailable for that
showtime. These reservations persist and are not automatically released;
reset them in the database after the demo if you need to reuse seats. The
payment page is non-functional and does not process payment or finalize an
order.

## Troubleshooting

- **Login fails:** Confirm the seeder printed `Demo admin account is ready`,
  you entered the same password at the hidden prompt, and you started the
  backend normally after seeding.
- **Seeder starts on every backend launch:** Check that `DEMO_SEED_ENABLED`
  was unset and that `DEMO_SEED_ENABLED=true` is not permanently set in the
  shell, IDE launch configuration, or environment.

Use the seeder only with a local/demo database. Do not expose these demo
accounts on a public deployment.
