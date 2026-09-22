CREATE DATABASE IF NOT EXISTS cinema_db;

USE cinema_db;

CREATE TABLE IF NOT EXISTS movies (
    movie_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100) NOT NULL,
    rating VARCHAR(20),
    description TEXT,
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    director VARCHAR(255),
    cast TEXT,
    producer VARCHAR(255)
);

INSERT INTO movies
(title, genre, rating, description, poster_url, trailer_url, status, director, cast, producer)
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