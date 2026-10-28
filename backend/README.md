# Cinema E-Booking System - Backend

## Overview

This directory contains the Spring Boot backend for the Cinema E-Booking System (CES).

The backend provides a REST API that connects the React frontend to the MySQL database.

The backend is responsible for:

- Registering customer accounts and confirming email addresses
- Retrieving movies from MySQL
- Searching movies by title
- Filtering movies by genre
- Filtering movies by status
- Retrieving an individual movie by ID
- Providing movie information to the React frontend

## Technology Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- MySQL
- Maven

## Backend Architecture

The backend follows a layered architecture:

    React Frontend
          |
          | HTTP / JSON
          v
    MovieController
          |
          v
    MovieService
          |
          v
    MovieRepository
          |
          v
    Movie Entity
          |
          v
    MySQL Database

### 1. Movie Entity

Location:

    src/main/java/com/cinema/cinema_backend/model/Movie.java

The `Movie` class represents a movie record in the MySQL `movies` table.

It contains:

- movieId
- title
- genre
- rating
- description
- posterUrl
- trailerUrl
- status
- director
- cast
- producer

The class uses JPA annotations to connect the Java object to the database table.

### 2. Movie Repository

Location:

    src/main/java/com/cinema/cinema_backend/repository/MovieRepository.java

`MovieRepository` extends Spring Data JPA's `JpaRepository`.

This provides standard database operations such as:

- `findAll()`
- `findById()`
- `save()`
- `delete()`

Custom repository methods are used for:

- Searching movies by title
- Filtering movies by genre
- Filtering movies by status

### 3. Movie Service

Location:

    src/main/java/com/cinema/cinema_backend/service/MovieService.java

The service layer contains the application's movie-related operations.

It communicates with `MovieRepository` and provides methods for:

- Getting all movies
- Searching movies
- Filtering movies by genre
- Filtering movies by status
- Getting a movie by ID

### 4. Movie Controller

Location:

    src/main/java/com/cinema/cinema_backend/controller/MovieController.java

The controller exposes the REST API endpoints used by the frontend.

Base URL:

    http://localhost:8080/api/movies

## API Endpoints

### Get all movies

    GET /api/movies

Example:

    http://localhost:8080/api/movies

Returns all movies stored in the database.

### Get a movie by ID

    GET /api/movies/{id}

Example:

    http://localhost:8080/api/movies/1

Returns the movie with the specified ID.

### Search movies by title

    GET /api/movies/search?title={title}

Example:

    http://localhost:8080/api/movies/search?title=Batman

The search is case-insensitive and searches for titles containing the provided text.

### Filter movies by genre

    GET /api/movies/genre?genre={genre}

Example:

    http://localhost:8080/api/movies/genre?genre=Animation

The genre filter is case-insensitive.

### Filter movies by status

    GET /api/movies/status?status={status}

Currently supported statuses are:

    CURRENTLY_RUNNING
    COMING_SOON

Examples:

    http://localhost:8080/api/movies/status?status=CURRENTLY_RUNNING

    http://localhost:8080/api/movies/status?status=COMING_SOON

## No-Match Results

When a search or filter does not find any movies, the API returns an empty JSON array:

    []

The React frontend can use this response to display an appropriate "No movies found" message.

## Database Connection

The backend connects to the MySQL database:

    Database: cinema_db_schema
    Host: localhost
    Port: 3306
    Username: root

The connection configuration is stored in:

    src/main/resources/application.properties

The password should remain local and should NOT be committed to GitHub.

The default database URL uses the database created by `database/cinema_db.sql`:

    jdbc:mysql://localhost:3306/cinema_db_schema

Override it with the `DB_URL` environment variable when using a different database.

Example configuration:

    spring.datasource.url=jdbc:mysql://localhost:3306/cinema_db_schema
    spring.datasource.username=root
    spring.datasource.password=YOUR_MYSQL_PASSWORD

    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true

## Running the Backend

### 1. Start MySQL

Make sure the MySQL server is running.

On macOS, the MySQL server can be started with:

    sudo /usr/local/mysql/support-files/mysql.server start

### 2. Navigate to the backend

    cd backend

### 3. Compile the project

    ./mvnw compile

A successful compilation should end with:

    BUILD SUCCESS

### 4. Start Spring Boot

    ./mvnw spring-boot:run

The backend runs on:

    http://localhost:8080

### 5. Test the API

Open the following URL in a browser:

    http://localhost:8080/api/movies

The response should contain the movies stored in MySQL.

## Current Sprint Scope

For Sprint 1 / Deliverable 2, the backend currently supports:

- Customer registration with email confirmation
- Server-side login sessions and logout
- Login and password reset by email
- Role-based customer/admin landing-page navigation
- Editable customer profile, one saved address, up to three encrypted payment cards, and database-backed favorites
- Dynamic movie retrieval from MySQL
- Movie search by title
- Movie filtering by genre
- Movie filtering by status
- Individual movie retrieval
- Movie poster and trailer URLs
- Currently Running and Coming Soon movie statuses

Showtimes and booking functionality are currently handled as prototype functionality in the frontend.

Full booking, ticket, payment processing, promotions, and administrative functionality will be implemented in later development stages. The admin portal is an authenticated navigation prototype; its menu sections are not functional management tools or protected management APIs.

## Customer Registration

Registration is available through:

    POST /api/auth/register

The request requires `firstName`, `lastName`, `email`, `password`, and `confirmPassword`. The optional `promotions` field defaults to `false`. Passwords must be at least 10 characters and include an uppercase letter, lowercase letter, number, and symbol.

New customers are saved in the `users` table with `userType=CUSTOMER` and `customerStatus=INACTIVE`. Passwords are BCrypt-hashed. The API sends an email with a single-use, 24-hour confirmation link; the link activates the account through:

    POST /api/auth/verify-email?token={token}

The verification token is stored as a SHA-256 hash in the JPA-managed `email_verification_tokens` table. Email delivery requires an SMTP server. Configure it using `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_AUTH`, and `SMTP_STARTTLS`; set `APP_MAIL_FROM` and `APP_FRONTEND_URL` for the sender address and public frontend URL. Local defaults target a development SMTP service on `localhost:1025` and the frontend on `http://localhost:5173`.

## Login and password reset

Login is available through `POST /api/auth/login` with an email and password:

    {
      "email": "customer@example.com",
      "password": "YourAccountPassword!1"
    }

Emails are matched case-insensitively. A successful response contains the
account's `email`, `name`, and `role` (`CUSTOMER` or `ADMIN`). Incorrect email
and password combinations return the same error. Unverified and suspended
customer accounts receive a status-specific message.

To reset a password, submit an email address to `POST /api/auth/forgot-password`.
The response is deliberately the same whether or not an account exists. If it
does, the configured mail sender sends a one-time link that expires after 30
minutes. The link opens the frontend reset form, which submits the token, new
password, and confirmation to `POST /api/auth/reset-password`. The new password
must meet the registration password requirements. Reset tokens are stored as
SHA-256 hashes and deleted after successful use.

After signing in, customers are routed to the movie home page and admins to the
admin portal prototype. The login creates an HTTP-only server session; logout
invalidates it, and `/api/admin/session` checks the current database role before
granting admin portal access. Its menu includes Manage Movies, Promotions,
Users, and Showtimes. Those menu sections are prototypes, not functional admin
tools. To prepare an admin demo, first register and verify a normal account,
then promote that account using a secured database client:

    UPDATE users
    SET userType = 'ADMIN', customerStatus = NULL, promotions = NULL
    WHERE email = 'admin@example.com';

The admin signs in with the password already set on that account. Do not use
this manual promotion workflow in production; no admin management APIs or
business data are implemented yet.

## Profile, cards, and favorites

Signed-in customers can use **My Profile** to edit their first and last names,
save one mailing/billing address, review or remove saved cards, and change their
password. The email address is displayed read-only. Changing personal
information sends a security notification to the account email. Changing the
password requires the current password and the new password is BCrypt-hashed.

Saved payment cards are limited to three per user. The app encrypts card
numbers using AES-256-GCM before database storage, displays only the card brand
and last four digits, and never asks for or stores a CVV. A base64-encoded
32-byte key is required. For local development, store it in
`src/main/resources/application.properties`, which is ignored by Git, or pass
it as an environment variable. Each developer must create and securely retain
their own key. This machine's key is already configured in its ignored local
settings; do not delete or replace it. Back it up in a password manager.
Generate a key once for another machine and save it in a password manager:

    openssl rand -base64 32

Before starting the backend, enter the saved key at the hidden prompt:

    read -rs "PAYMENT_CARD_ENCRYPTION_KEY?Saved payment-card key (input hidden): "
    printf '\n'
    export PAYMENT_CARD_ENCRYPTION_KEY

Keep the same key across restarts and back it up securely; replacing or losing
it makes stored card numbers unrecoverable. Do not commit the key. If existing
payment-card rows contain plaintext numbers, the backend encrypts those rows
on startup and requires this key to be configured before it can finish startup.
Use HTTPS and a managed secrets store outside local demos; application-level
encryption does not make this prototype a payment processor or PCI-compliant
card vault.

Favorites are stored per signed-in user in the `favorites` table. Selecting the
heart on a movie saves or removes it, and the Favorites filter shows saved
movies across sessions. Guests may still use the existing browser-local
favorites.

## Preparing demo accounts

The optional demo seeder is disabled by default. To prepare the demo accounts
before the presentation, stop the normal backend, then start it once with
`DEMO_SEED_ENABLED=true` and `DEMO_SEED_PASSWORD` set. On macOS, enter the
password at a hidden prompt:

    cd backend
    read -rs "DEMO_SEED_PASSWORD?Demo account password (input hidden): "
    printf '\n'
    export DEMO_SEED_PASSWORD
    export DEMO_SEED_ENABLED=true
    ./mvnw spring-boot:run

Wait for the `Demo data is ready` message, stop the backend with `Control+C`,
and clear the temporary environment variables:

    unset DEMO_SEED_PASSWORD DEMO_SEED_ENABLED

Start the backend normally after seeding. The seeder creates or updates the
admin `kndllhaynes@gmail.com`, verified favorite user `kch55499@uga.edu`, and
verified card user `kendallhaynes7730@gmail.com`. It stores a favorite for the
favorite user and adds fake test cards, encrypted with the configured
card-encryption key, until the card user has three. Existing addresses and
saved cards are preserved. Re-running the seeder does not duplicate the
favorite or exceed the three-card limit. Never use real payment-card numbers
for this demo.

Session cookies are HTTP-only and use `SameSite=Lax`. The Vite development
server proxies `/api` requests to the backend so the browser uses the same
origin for the session cookie. In a deployment, use HTTPS and configure the
frontend/backend under a same-site origin; set `SESSION_COOKIE_SECURE=true`.

### Backend launch troubleshooting

Start the backend from this directory with the Maven wrapper:

    ./mvnw spring-boot:run

Do not launch `CinemaBackendApplication` with only `target/classes` on the Java
classpath. That omits Spring Boot and other dependency JARs and causes
`NoClassDefFoundError: org/springframework/boot/SpringApplication`. The Maven
wrapper resolves and supplies the full runtime classpath.

### Send email through Gmail

The address entered by the customer in the form is the recipient. Gmail is only
the account the backend sends through. For example, to send through
`cinemaebooking10@gmail.com`, first enable 2-Step Verification for that Google
account and create a Google App Password. Do not use your regular Google
password or commit the App Password.

In the macOS Terminal (default `zsh`), enter the App Password at the hidden
prompt and start the backend in that same terminal:

    cd backend
    read -rs "SMTP_PASSWORD?Gmail App Password (without spaces; input hidden): "
    printf '\n'
    export SMTP_PASSWORD
    export SMTP_HOST=smtp.gmail.com SMTP_PORT=587
    export SMTP_USERNAME=cinemaebooking10@gmail.com APP_MAIL_FROM=cinemaebooking10@gmail.com
    export SMTP_AUTH=true SMTP_STARTTLS=true
    ./mvnw spring-boot:run

Keep this terminal open while testing. The password is held only in that
terminal's environment and is not added to the project or shell history. Stop
the currently running backend before starting it this way. To return to
Mailpit, start the backend without these Gmail settings so it uses the local
defaults.

For local testing, install and start [Mailpit](https://mailpit.axllent.org/):

    brew install mailpit
    mailpit --listen 127.0.0.1:8025 --smtp 127.0.0.1:1025

Open `http://127.0.0.1:8025` to inspect confirmation emails. The local-only bind keeps the test SMTP server and its inbox off the network.