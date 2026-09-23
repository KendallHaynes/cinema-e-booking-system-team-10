# Cinema E-Booking System - Backend

## Overview

This directory contains the Spring Boot backend for the Cinema E-Booking System (CES).

The backend provides a REST API that connects the React frontend to the MySQL database.

The backend is responsible for:

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

    Database: cinema_db
    Host: localhost
    Port: 3306
    Username: root

The connection configuration is stored in:

    src/main/resources/application.properties

The password should remain local and should NOT be committed to GitHub.

Example configuration:

    spring.datasource.url=jdbc:mysql://localhost:3306/cinema_db
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

- Dynamic movie retrieval from MySQL
- Movie search by title
- Movie filtering by genre
- Movie filtering by status
- Individual movie retrieval
- Movie poster and trailer URLs
- Currently Running and Coming Soon movie statuses

Showtimes and booking functionality are currently handled as prototype functionality in the frontend.

Full booking, ticket, payment, seat reservation, favorites, promotions, and administrative functionality will be implemented in later development stages.