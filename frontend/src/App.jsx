import { useEffect, useState } from "react";

function App() {
  const [movies, setMovies] = useState([]);

  useEffect(() => {
    fetch("http://localhost:8080/api/movies")
      .then((response) => response.json())
      .then((data) => {
        setMovies(data);
      })
      .catch((error) => {
        console.error("Error fetching movies:", error);
      });
  }, []);

  return (
    <div>
      <h1>Cinema E-Booking System</h1>

      <h2>Movies</h2>

      {movies.map((movie) => (
        <div key={movie.movieId}>
          <h3>{movie.title}</h3>
          <p>Genre: {movie.genre}</p>
          <p>Rating: {movie.rating}</p>
          <p>Status: {movie.status}</p>
        </div>
      ))}
    </div>
  );
}

export default App;