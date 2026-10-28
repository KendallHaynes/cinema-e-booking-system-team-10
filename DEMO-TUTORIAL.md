# Cinema E-Booking System Demo Tutorial

This guide prepares the local database before the presentation so the demo
accounts and sample profile data are already available when the demo begins.
The demo seeder is opt-in and does not run during a normal backend start.

## Demo accounts

| Account | Email | Demo data |
|---|---|---|
| Admin | `kndllhaynes@gmail.com` | Admin role |
| Favorite customer | `kch55499@uga.edu` | Verified customer with a saved movie favorite |
| Card customer | `kendallhaynes7730@gmail.com` | Verified customer with a profile address and up to three saved cards |

All three accounts use the shared demo password you selected. It is not
written in this file or stored in source control. Enter it when prompted during
the setup below.

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
   Demo data is ready
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

- Creates the three accounts if their email addresses are new.
- Updates matching accounts to the requested demo names, roles, active customer
  status, and shared password. Passwords are stored as BCrypt hashes.
- Makes both customer accounts verified/active; they do not need email
  confirmation to sign in.
- Adds a movie to the favorite user's favorites, if one is not already saved.
- Creates a demo address for the card account only if it does not already have
  one.
- Adds fake, demo-only payment-card test values until the card account has
  three cards. New card numbers are encrypted with the configured local
  payment-card encryption key.

The seeder is safe to rerun: it does not duplicate the favorite or add cards
past the three-card limit. It preserves any existing address and saved cards.
Therefore, if the card account already has saved cards, those are retained and
only missing card slots are filled. Never use real payment-card numbers in
this demo.

## Sign in during the demo

1. Open the frontend and choose **Login**.
2. Sign in with one of the emails in the account table and the shared demo
   password.
3. The admin account opens the admin portal. The two customer accounts open
   the customer experience; use **Favorites** to show the saved movie or
   **My Profile** to show the address and saved cards.

The admin portal is a navigation prototype; its management sections are not
fully implemented.

## Troubleshooting

- **Login fails:** Confirm the seeder printed `Demo data is ready`, that you
  entered the same password at the hidden prompt, and that you started the
  backend normally after seeding.
- **Favorite list is empty:** The database needs at least one movie. Confirm
  the movies are present in the database, then rerun the seeder.
- **Fewer than three cards appear:** Check the backend startup output for a
  card-encryption or database error. The local encryption key must remain
  configured and must not be changed after cards are encrypted.
- **Seeder starts on every backend launch:** Check that `DEMO_SEED_ENABLED`
  was unset and that `DEMO_SEED_ENABLED=true` is not permanently set in the
  shell, IDE launch configuration, or environment.

Use the seeder only with a local/demo database. Do not expose these demo
accounts on a public deployment.
