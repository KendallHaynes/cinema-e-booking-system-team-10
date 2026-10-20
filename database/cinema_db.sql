CREATE DATABASE IF NOT EXISTS cinema_db_schema;
USE cinema_db_schema;
-- =========================================
-- MOVIES
-- =========================================

CREATE TABLE IF NOT EXISTS movies (
    movie_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100) NOT NULL,
    rating VARCHAR(20),
    description TEXT,
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    director VARCHAR(255),
    `cast` TEXT,
    producer VARCHAR(255)
);

-- =========================================
-- USERS (CUSTOMERS AND ADMINS)
-- =========================================

CREATE TABLE IF NOT EXISTS users (
    userID BIGINT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(100) NOT NULL,
    lastName VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    userType VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    customerStatus VARCHAR(20) DEFAULT 'ACTIVE',
    promotions BOOLEAN DEFAULT FALSE,

    CONSTRAINT chk_user_type
        CHECK (userType IN ('CUSTOMER', 'ADMIN')),

    CONSTRAINT chk_customer_status
        CHECK (
            customerStatus IS NULL
            OR customerStatus IN ('ACTIVE', 'SUSPENDED', 'INACTIVE')
        ),

    CONSTRAINT chk_admin_customer_fields
        CHECK (
            (userType = 'CUSTOMER'
                AND customerStatus IS NOT NULL
                AND promotions IS NOT NULL)
            OR
            (userType = 'ADMIN'
                AND customerStatus IS NULL
                AND promotions IS NULL)
        )
);

-- =========================================
-- PROMOTIONS
-- =========================================

CREATE TABLE IF NOT EXISTS promotions (
    promotionID BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    discountPercentage DECIMAL(5,2) NOT NULL,
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,

    CONSTRAINT chk_promotion_discount
        CHECK (discountPercentage > 0 AND discountPercentage <= 100),

    CONSTRAINT chk_promotion_dates
        CHECK (endDate >= startDate)
);

-- =========================================
-- SHOWROOMS
-- =========================================

CREATE TABLE IF NOT EXISTS showrooms (
    roomID BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    CONSTRAINT chk_showroom_capacity CHECK (capacity > 0)
);

-- =========================================
-- SEATS
-- =========================================

CREATE TABLE IF NOT EXISTS seats (
    seatID BIGINT AUTO_INCREMENT PRIMARY KEY,
    `row` VARCHAR(10) NOT NULL,
    seatNumber INT NOT NULL,


    CONSTRAINT chk_seat_number
        CHECK (seatNumber > 0)
);

-- =========================================
-- SHOWS
-- =========================================

CREATE TABLE IF NOT EXISTS shows (
    showID BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateTime DATETIME NOT NULL,
    movieID BIGINT NOT NULL,
    roomID BIGINT NOT NULL,

    CONSTRAINT unique_room_datetime
        UNIQUE (roomID, dateTime),

    CONSTRAINT fk_shows_movie
        FOREIGN KEY (movieID)
        REFERENCES movies(movie_id),

    CONSTRAINT fk_shows_showroom
        FOREIGN KEY (roomID)
        REFERENCES showrooms(roomID)
);

-- =========================================
-- SHOW SEATS
-- =========================================

CREATE TABLE IF NOT EXISTS show_seats (
    showSeatID BIGINT AUTO_INCREMENT PRIMARY KEY,
    showID BIGINT NOT NULL,
    seatID BIGINT NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_show_seats_show
        FOREIGN KEY (showID)
        REFERENCES shows(showID)
        ON DELETE CASCADE,

    CONSTRAINT fk_show_seats_seat
        FOREIGN KEY (seatID)
        REFERENCES seats(seatID),

    CONSTRAINT unique_show_seat
        UNIQUE (showID, seatID)
);

-- =========================================
-- PAYMENT CARDS
-- =========================================

CREATE TABLE IF NOT EXISTS payment_cards (
    cardID BIGINT AUTO_INCREMENT PRIMARY KEY,
    cardNumber VARCHAR(255) NOT NULL,
    expirationDate DATE NOT NULL,
    billingAddress VARCHAR(255) NOT NULL,
    userID BIGINT NOT NULL,

    CONSTRAINT fk_payment_cards_user
        FOREIGN KEY (userID)
        REFERENCES users(userID)
        ON DELETE CASCADE
);

-- =========================================
-- BOOKINGS
-- =========================================

CREATE TABLE IF NOT EXISTS bookings (
    bookingID BIGINT AUTO_INCREMENT PRIMARY KEY,
    bookingDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL,
    userID BIGINT NOT NULL,
    cardID BIGINT NOT NULL,
    promotionID BIGINT NULL,

    CONSTRAINT chk_booking_total
        CHECK (total >= 0),

    CONSTRAINT fk_bookings_user
        FOREIGN KEY (userID)
        REFERENCES users(userID),



    CONSTRAINT fk_bookings_card
        FOREIGN KEY (cardID)
        REFERENCES payment_cards(cardID),

    CONSTRAINT fk_bookings_promotion
        FOREIGN KEY (promotionID)
        REFERENCES promotions(promotionID)
        ON DELETE SET NULL
);

-- =========================================
-- TICKETS
-- =========================================

CREATE TABLE IF NOT EXISTS tickets (
    ticketID BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticketType VARCHAR(10) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    bookingID BIGINT NOT NULL,
    showSeatID BIGINT NOT NULL,

    CONSTRAINT chk_ticket_type
        CHECK (ticketType IN ('ADULT', 'CHILD', 'SENIOR')),

    CONSTRAINT chk_ticket_price
        CHECK (price >= 0),

    CONSTRAINT fk_tickets_booking
        FOREIGN KEY (bookingID)
        REFERENCES bookings(bookingID)
        ON DELETE CASCADE,

    CONSTRAINT fk_tickets_show_seat
        FOREIGN KEY (showSeatID)
        REFERENCES show_seats(showSeatID),

    CONSTRAINT unique_ticket_show_seat
        UNIQUE (showSeatID)
);

-- =========================================
-- FAVORITES
-- =========================================

CREATE TABLE IF NOT EXISTS favorites (
    userID BIGINT NOT NULL,
    movieID BIGINT NOT NULL,
    dateAdded DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (userID, movieID),

    CONSTRAINT fk_favorites_user
        FOREIGN KEY (userID)
        REFERENCES users(userID)
        ON DELETE CASCADE,

    CONSTRAINT fk_favorites_movie
        FOREIGN KEY (movieID)
        REFERENCES movies(movie_id)
        ON DELETE CASCADE
);

-- =========================================
-- SAMPLE MOVIES
-- =========================================
-- If you rerun this script, these INSERT statements can add duplicate movies.
-- Run them only when the movies table is empty, or use a separate seed script.

INSERT INTO movies
(title, genre, rating, description, poster_url, trailer_url, status, director, `cast`, producer)
VALUES
(
    'The Batman',
    'Action',
    'PG-13',
    'Batman ventures into Gotham City to uncover corruption and confront a serial killer known as the Riddler.',
    'https://img.youtube.com/vi/mqqft2x_Aa4/maxresdefault.jpg',
    'https://www.youtube.com/embed/mqqft2x_Aa4',
    'CURRENTLY_RUNNING',
    'Matt Reeves',
    'Robert Pattinson, Zoë Kravitz, Paul Dano',
    'Warner Bros. Pictures'
),
(
    'Interstellar',
    'Science Fiction',
    'PG-13',
    'A group of explorers travels through a wormhole in space in search of a new home for humanity.',
    'https://img.youtube.com/vi/zSWdZVtXT7E/maxresdefault.jpg',
    'https://www.youtube.com/embed/zSWdZVtXT7E',
    'CURRENTLY_RUNNING',
    'Christopher Nolan',
    'Matthew McConaughey, Anne Hathaway, Jessica Chastain',
    'Paramount Pictures'
),
(
    'Dune: Part Two',
    'Science Fiction',
    'PG-13',
    'Paul Atreides joins Chani and the Fremen while seeking revenge against those who destroyed his family.',
    'https://img.youtube.com/vi/Way9Dexny3w/maxresdefault.jpg',
    'https://www.youtube.com/embed/Way9Dexny3w',
    'CURRENTLY_RUNNING',
    'Denis Villeneuve',
    'Timothée Chalamet, Zendaya, Rebecca Ferguson',
    'Warner Bros. Pictures'
),
(
    'Inside Out 2',
    'Animation',
    'PG',
    'Riley enters her teenage years as new emotions arrive and challenge the emotions already living inside her mind.',
    'https://img.youtube.com/vi/LEjhY15eCx0/maxresdefault.jpg',
    'https://www.youtube.com/embed/LEjhY15eCx0',
    'CURRENTLY_RUNNING',
    'Kelsey Mann',
    'Amy Poehler, Maya Hawke, Kensington Tallman',
    'Pixar Animation Studios'
),
(
    'Oppenheimer',
    'Drama',
    'R',
    'The story of J. Robert Oppenheimer and the development of the first atomic bomb.',
    'https://img.youtube.com/vi/uYPbbksJxIg/maxresdefault.jpg',
    'https://www.youtube.com/embed/uYPbbksJxIg',
    'CURRENTLY_RUNNING',
    'Christopher Nolan',
    'Cillian Murphy, Emily Blunt, Robert Downey Jr.',
    'Universal Pictures'
),
(
    'Spider-Man: Across the Spider-Verse',
    'Animation',
    'PG',
    'Miles Morales travels across the multiverse and encounters a team of Spider-People protecting reality.',
    'https://img.youtube.com/vi/cqGjhVJWtEg/maxresdefault.jpg',
    'https://www.youtube.com/embed/cqGjhVJWtEg',
    'CURRENTLY_RUNNING',
    'Joaquim Dos Santos',
    'Shameik Moore, Hailee Steinfeld, Brian Tyree Henry',
    'Sony Pictures Animation'
),
(
    'Barbie',
    'Comedy',
    'PG-13',
    'Barbie leaves Barbieland and enters the real world, where she begins questioning her understanding of life.',
    'https://img.youtube.com/vi/pBk4NYhWNMM/maxresdefault.jpg',
    'https://www.youtube.com/embed/pBk4NYhWNMM',
    'COMING_SOON',
    'Greta Gerwig',
    'Margot Robbie, Ryan Gosling, America Ferrera',
    'Warner Bros. Pictures'
),
(
    'Wicked',
    'Fantasy',
    'PG',
    'Two students at Shiz University form an unexpected friendship that changes the course of their lives.',
    'https://img.youtube.com/vi/6COmYeLsz4c/maxresdefault.jpg',
    'https://www.youtube.com/embed/6COmYeLsz4c',
    'COMING_SOON',
    'Jon M. Chu',
    'Cynthia Erivo, Ariana Grande, Jonathan Bailey',
    'Universal Pictures'
),
(
    'Moana 2',
    'Animation',
    'PG',
    'Moana sets out on a new voyage across the ocean after receiving an unexpected call from her ancestors.',
    'https://img.youtube.com/vi/hDZ7y8RP5HE/maxresdefault.jpg',
    'https://www.youtube.com/embed/hDZ7y8RP5HE',
    'COMING_SOON',
    'David G. Derrick Jr.',
    'Auliʻi Cravalho, Dwayne Johnson, Hualālai Chung',
    'Walt Disney Animation Studios'
),
(
    'Deadpool & Wolverine',
    'Action',
    'R',
    'Deadpool is recruited into a mission that brings him together with Wolverine.',
    'https://img.youtube.com/vi/73_1biulkYk/maxresdefault.jpg',
    'https://www.youtube.com/embed/73_1biulkYk',
    'COMING_SOON',
    'Shawn Levy',
    'Ryan Reynolds, Hugh Jackman, Emma Corrin',
    'Marvel Studios'
),
(
    'The Super Mario Bros. Movie',
    'Animation',
    'PG',
    'Mario and Luigi are transported to a mysterious world and become involved in a battle to save the Mushroom Kingdom.',
    'https://img.youtube.com/vi/TnGl01FkMMo/maxresdefault.jpg',
    'https://www.youtube.com/embed/TnGl01FkMMo',
    'COMING_SOON',
    'Aaron Horvath',
    'Chris Pratt, Anya Taylor-Joy, Charlie Day',
    'Illumination'
);
