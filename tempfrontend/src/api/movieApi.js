// API Client for Cinema E-Booking System Backend
const BASE_URL = 'http://localhost:8080/api/movies';

// Seed data fallback from database/cinema_db.sql
export const FALLBACK_MOVIES = [
  {
    movieId: 1,
    title: 'The Batman',
    genre: 'Action',
    rating: 'PG-13',
    description: 'Batman ventures into Gotham City to uncover corruption and confront a serial killer known as the Riddler.',
    posterUrl: 'https://img.youtube.com/vi/mqqft2x_Aa4/maxresdefault.jpg',
    trailerUrl: 'https://www.youtube.com/embed/mqqft2x_Aa4',
    status: 'CURRENTLY_RUNNING',
    director: 'Matt Reeves',
    cast: 'Robert Pattinson, Zoë Kravitz, Paul Dano',
    producer: 'Warner Bros. Pictures'
  },
  {
    movieId: 2,
    title: 'Interstellar',
    genre: 'Science Fiction',
    rating: 'PG-13',
    description: 'A group of explorers travels through a wormhole in space in search of a new home for humanity.',
    posterUrl: 'https://img.youtube.com/vi/zSWdZVtXT7E/maxresdefault.jpg',
    trailerUrl: 'https://www.youtube.com/embed/zSWdZVtXT7E',
    status: 'CURRENTLY_RUNNING',
    director: 'Christopher Nolan',
    cast: 'Matthew McConaughey, Anne Hathaway, Jessica Chastain',
    producer: 'Paramount Pictures'
  },
  {
    movieId: 3,
    title: 'Dune: Part Two',
    genre: 'Science Fiction',
    rating: 'PG-13',
    description: 'Paul Atreides joins Chani and the Fremen while seeking revenge against those who destroyed his family.',
    posterUrl: 'https://img.youtube.com/vi/Way9Dexny3w/maxresdefault.jpg',
    trailerUrl: 'https://www.youtube.com/embed/Way9Dexny3w',
    status: 'CURRENTLY_RUNNING',
    director: 'Denis Villeneuve',
    cast: 'Timothée Chalamet, Zendaya, Rebecca Ferguson',
    producer: 'Warner Bros. Pictures'
  },
  {
    movieId: 4,
    title: 'Inside Out 2',
    genre: 'Animation',
    rating: 'PG',
    description: 'Riley enters her teenage years as new emotions arrive and challenge the emotions already living inside her mind.',
    posterUrl: 'https://img.youtube.com/vi/LEjhY15eCx0/maxresdefault.jpg',
    trailerUrl: 'https://www.youtube.com/embed/LEjhY15eCx0',
    status: 'CURRENTLY_RUNNING',
    director: 'Kelsey Mann',
    cast: 'Amy Poehler, Maya Hawke, Kensington Tallman',
    producer: 'Pixar Animation Studios'
  },
  {
    movieId: 5,
    title: 'Oppenheimer',
    genre: 'Drama',
    rating: 'R',
    description: 'The story of J. Robert Oppenheimer and the development of the first atomic bomb.',
    posterUrl: 'https://img.youtube.com/vi/uYPbbksJxIg/maxresdefault.jpg',
    trailerUrl: 'https://www.youtube.com/embed/uYPbbksJxIg',
    status: 'CURRENTLY_RUNNING',
    director: 'Christopher Nolan',
    cast: 'Cillian Murphy, Emily Blunt, Robert Downey Jr.',
    producer: 'Universal Pictures'
  },
  {
    movieId: 6,
    title: 'Spider-Man: Across the Spider-Verse',
    genre: 'Animation',
    rating: 'PG',
    description: 'Miles Morales travels across the multiverse and encounters a team of Spider-People protecting reality.',
    posterUrl: 'https://img.youtube.com/vi/cqGjhVJWtEg/maxresdefault.jpg',
    trailerUrl: 'https://www.youtube.com/embed/cqGjhVJWtEg',
    status: 'CURRENTLY_RUNNING',
    director: 'Joaquim Dos Santos',
    cast: 'Shameik Moore, Hailee Steinfeld, Brian Tyree Henry',
    producer: 'Sony Pictures Animation'
  },
  {
    movieId: 7,
    title: 'Barbie',
    genre: 'Comedy',
    rating: 'PG-13',
    description: 'Barbie leaves Barbieland and enters the real world, where she begins questioning her understanding of life.',
    posterUrl: 'https://img.youtube.com/vi/pBk4NYhWNMM/maxresdefault.jpg',
    trailerUrl: 'https://www.youtube.com/embed/pBk4NYhWNMM',
    status: 'COMING_SOON',
    director: 'Greta Gerwig',
    cast: 'Margot Robbie, Ryan Gosling, America Ferrera',
    producer: 'Warner Bros. Pictures'
  },
  {
    movieId: 8,
    title: 'Wicked',
    genre: 'Fantasy',
    rating: 'PG',
    description: 'Two students at Shiz University form an unexpected friendship that changes the course of their lives.',
    posterUrl: 'https://img.youtube.com/vi/6COmYeLsz4c/maxresdefault.jpg',
    trailerUrl: 'https://www.youtube.com/embed/6COmYeLsz4c',
    status: 'COMING_SOON',
    director: 'Jon M. Chu',
    cast: 'Cynthia Erivo, Ariana Grande, Jonathan Bailey',
    producer: 'Universal Pictures'
  },
  {
    movieId: 9,
    title: 'Moana 2',
    genre: 'Animation',
    rating: 'PG',
    description: 'Moana sets out on a new voyage across the ocean after receiving an unexpected call from her ancestors.',
    posterUrl: 'https://img.youtube.com/vi/hDZ7y8RP5HE/maxresdefault.jpg',
    trailerUrl: 'https://www.youtube.com/embed/hDZ7y8RP5HE',
    status: 'COMING_SOON',
    director: 'David G. Derrick Jr.',
    cast: 'Auliʻi Cravalho, Dwayne Johnson, Hualālai Chung',
    producer: 'Walt Disney Animation Studios'
  },
  {
    movieId: 10,
    title: 'Deadpool & Wolverine',
    genre: 'Action',
    rating: 'R',
    description: 'Deadpool is recruited into a mission that brings him together with Wolverine.',
    posterUrl: 'https://img.youtube.com/vi/73_1biulkYk/maxresdefault.jpg',
    trailerUrl: 'https://www.youtube.com/embed/73_1biulkYk',
    status: 'COMING_SOON',
    director: 'Shawn Levy',
    cast: 'Ryan Reynolds, Hugh Jackman, Emma Corrin',
    producer: 'Marvel Studios'
  },
  {
    movieId: 11,
    title: 'The Super Mario Bros. Movie',
    genre: 'Animation',
    rating: 'PG',
    description: 'Mario and Luigi are transported to a mysterious world and become involved in a battle to save the Mushroom Kingdom.',
    posterUrl: 'https://img.youtube.com/vi/TnGl01FkMMo/maxresdefault.jpg',
    trailerUrl: 'https://www.youtube.com/embed/TnGl01FkMMo',
    status: 'COMING_SOON',
    director: 'Aaron Horvath',
    cast: 'Chris Pratt, Anya Taylor-Joy, Charlie Day',
    producer: 'Illumination'
  }
];

// Helper to normalize any differences in field naming
function normalizeMovie(movie) {
  if (!movie) return null;
  return {
    ...movie,
    id: movie.movieId || movie.id,
    movieId: movie.movieId || movie.id,
    posterUrl: movie.posterUrl || movie.poster_url,
    trailerUrl: movie.trailerUrl || movie.trailer_url
  };
}

/**
 * Fetch all movies from backend DB, fallback to seed data if server is offline
 */
export async function getAllMovies() {
  try {
    const res = await fetch(BASE_URL, { signal: AbortSignal.timeout(3000) });
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
    const data = await res.json();
    return { data: data.map(normalizeMovie), isLive: true };
  } catch {
    return { data: FALLBACK_MOVIES.map(normalizeMovie), isLive: false };
  }
}

/**
 * Fetch single movie by ID
 */
export async function getMovieById(id) {
  try {
    const res = await fetch(`${BASE_URL}/${id}`, { signal: AbortSignal.timeout(3000) });
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
    const data = await res.json();
    return { data: normalizeMovie(data), isLive: true };
  } catch {
    const movie = FALLBACK_MOVIES.find((m) => m.movieId === Number(id));
    return { data: normalizeMovie(movie), isLive: false };
  }
}

/**
 * Search movies by title
 */
export async function searchMoviesByTitle(title) {
  if (!title || !title.trim()) {
    return getAllMovies();
  }
  const query = title.trim();
  try {
    const res = await fetch(`${BASE_URL}/search?title=${encodeURIComponent(query)}`, {
      signal: AbortSignal.timeout(3000)
    });
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
    const data = await res.json();
    return { data: data.map(normalizeMovie), isLive: true };
  } catch {
    const filtered = FALLBACK_MOVIES.filter((m) =>
      m.title.toLowerCase().includes(query.toLowerCase())
    );
    return { data: filtered.map(normalizeMovie), isLive: false };
  }
}

/**
 * Filter movies by genre
 */
export async function filterMoviesByGenre(genre) {
  if (!genre || genre === 'ALL') {
    return getAllMovies();
  }
  try {
    const res = await fetch(`${BASE_URL}/genre?genre=${encodeURIComponent(genre)}`, {
      signal: AbortSignal.timeout(3000)
    });
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
    const data = await res.json();
    return { data: data.map(normalizeMovie), isLive: true };
  } catch {
    const filtered = FALLBACK_MOVIES.filter(
      (m) => m.genre.toLowerCase() === genre.toLowerCase()
    );
    return { data: filtered.map(normalizeMovie), isLive: false };
  }
}

/**
 * Get movies by status (CURRENTLY_RUNNING or COMING_SOON)
 */
export async function getMoviesByStatus(status) {
  try {
    const res = await fetch(`${BASE_URL}/status?status=${encodeURIComponent(status)}`, {
      signal: AbortSignal.timeout(3000)
    });
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
    const data = await res.json();
    return { data: data.map(normalizeMovie), isLive: true };
  } catch {
    const filtered = FALLBACK_MOVIES.filter((m) => m.status === status);
    return { data: filtered.map(normalizeMovie), isLive: false };
  }
}
