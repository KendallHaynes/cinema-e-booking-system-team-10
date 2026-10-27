# Cinema E-Booking System - Database

## Overview

This directory contains the MySQL database setup for the Cinema E-Booking System (CES).

The database stores cinema catalog, user, scheduling, and booking information used by the Spring Boot backend.

Database name:

    cinema_db_schema

Database management system:

    MySQL

## Current Database Structure

The schema defines movies, users, email-verification tokens, promotions,
showrooms, seats, shows, show seats, customer addresses, payment cards,
bookings, tickets, and favorites.

## Movies Table

The table contains the following columns:

| Column | Type | Description |
|---|---|---|
| movie_id | BIGINT | Unique identifier for each movie |
| title | VARCHAR(255) | Movie title |
| genre | VARCHAR(100) | Movie genre |
| rating | VARCHAR(20) | Movie rating |
| description | TEXT | Movie description |
| poster_url | VARCHAR(500) | URL for the movie poster |
| trailer_url | VARCHAR(500) | Embedded YouTube trailer URL |
| status | VARCHAR(30) | Current movie status |
| director | VARCHAR(255) | Movie director |
| cast | TEXT | Main cast information |
| producer | VARCHAR(255) | Movie producer |

## Primary Key

`movie_id` is the primary key.

It uniquely identifies each movie in the table.

The ID is automatically generated using:

    AUTO_INCREMENT

## Movie Status

The `status` column is used during Sprint 1 to separate movies into two categories:

    CURRENTLY_RUNNING

and:

    COMING_SOON

This allows the backend to retrieve movies for the appropriate sections of the Home page.

In a later version, movie status can be determined dynamically from scheduled showtimes.

## Poster and Trailer URLs

Movie posters are referenced using URLs rather than storing image files directly in the database.

Example poster URL:

    https://img.youtube.com/vi/VIDEO_ID/maxresdefault.jpg

Movie trailers use embedded YouTube URLs.

Example:

    https://www.youtube.com/embed/VIDEO_ID

The frontend uses these URLs to display posters and embedded trailers.

## Current Seed Data

The database currently contains 11 movies.

The movies include multiple genres and both supported movie statuses.

### Currently Running

- The Batman
- Interstellar
- Dune: Part Two
- Inside Out 2
- Oppenheimer
- Spider-Man: Across the Spider-Verse

### Coming Soon

- Barbie
- Wicked
- Moana 2
- Deadpool & Wolverine
- The Super Mario Bros. Movie

## Database Setup

### 1. Start MySQL

On macOS:

    sudo /usr/local/mysql/support-files/mysql.server start

### 2. Open MySQL

    mysql -u root -p

Enter the MySQL root password when prompted.

### 3. Create the database

The SQL file creates the database and all defined tables, then inserts the movie seed data.

The database created by the current SQL file is:

    cinema_db_schema

### 4. Select the database

    USE cinema_db_schema;

### 5. Check the movies table

    SHOW TABLES;

You should see tables including:

    movies
    users
    favorites

### 6. View the movie data

    SELECT * FROM movies;

## SQL File

The database initialization and seed data are stored in:

    cinema_db.sql

This file can be used to recreate the database structure and populate the movie data.

## Useful SQL Queries

### View all movies

    SELECT * FROM movies;

### Search by title

    SELECT *
    FROM movies
    WHERE title LIKE '%Batman%';

### Filter by genre

    SELECT *
    FROM movies
    WHERE genre = 'Animation';

### View currently running movies

    SELECT *
    FROM movies
    WHERE status = 'CURRENTLY_RUNNING';

### View coming soon movies

    SELECT *
    FROM movies
    WHERE status = 'COMING_SOON';

### Count movies

    SELECT COUNT(*)
    FROM movies;

### Find a movie by ID

    SELECT *
    FROM movies
    WHERE movie_id = 1;

## Relationship to the Backend

The Spring Boot backend connects to this database using:

    Spring Data JPA
    Hibernate
    MySQL Connector/J

The flow is:

    MySQL movies table
          |
          v
    Movie.java
          |
          v
    MovieRepository
          |
          v
    MovieService
          |
          v
    MovieController
          |
          v
    React Frontend

The database is therefore the source of truth for the movie information displayed by the application.

## Users Table

The `users` table stores customer and administrator accounts. Account types are
`CUSTOMER` or `ADMIN`; customer statuses are `ACTIVE`, `SUSPENDED`, or
`INACTIVE`. New registrations are created as `CUSTOMER` accounts with
`INACTIVE` status until the user confirms their email. The backend stores a
secure password hash in the `password` column, never the plaintext password.

The `email_verification_tokens` table stores only a hash of each single-use
verification token and its expiration time.

The `customer_addresses` table enforces at most one saved address per user.
The `payment_cards` table permits up to three saved cards per user through
backend validation and stores encrypted card numbers after the application
starts. The profile API returns only the card brand and last four digits.
Favorites are linked to both the user and movie and are stored in the
`favorites` table.

### Migrate an existing registration schema

If registration fails with a MySQL error such as `Field 'first_name' doesn't
have a default value`, the existing `users` table has legacy snake_case columns
alongside the current camelCase columns. Back up the database, then run
`migrate_registration_columns.sql` once against `cinema_db_schema`. It copies
legacy values into the current columns and removes the obsolete duplicates.
New databases created from `cinema_db.sql` do not need this migration.

## Current Scope

The database supports movie browsing and defines tables for:

- Dynamic movie retrieval
- Movie search
- Genre filtering
- Currently Running movies
- Coming Soon movies
- Movie details
- Payment cards
- Seats
- Shows and show seats
- Bookings
- Tickets
- Favorites
- Promotions
- Users and showrooms