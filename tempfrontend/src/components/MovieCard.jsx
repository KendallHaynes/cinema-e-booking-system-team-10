import { useState } from 'react';
import { useMovieContext } from '../context/MovieContext';

export default function MovieCard({ movie }) {
  const { openMovieDetail, isFavorite, toggleFavorite } = useMovieContext();
  const [imageError, setImageError] = useState(false);

  const favorited = isFavorite(movie.movieId);

  // Poster fallback generator if remote image fails
  const getPosterSrc = () => {
    if (imageError || !movie.posterUrl) {
      // Fallback high quality cinema placeholder
      return `https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&auto=format&fit=crop&q=80`;
    }
    return movie.posterUrl;
  };

  const handleStarClick = (e) => {
    e.stopPropagation();
    toggleFavorite(movie.movieId, movie.title);
  };

  return (
    <article
      className="ces-movie-card"
      onClick={() => openMovieDetail(movie)}
      tabIndex={0}
      role="button"
      aria-label={`View details for ${movie.title}`}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          openMovieDetail(movie);
        }
      }}
    >
      {/* Poster Image Container */}
      <div className="card-poster-wrapper">
        <img
          src={getPosterSrc()}
          alt={movie.title}
          className="card-poster-img"
          loading="lazy"
          onError={() => setImageError(true)}
        />
        <div className="card-overlay-gradient"></div>

        {/* Status Tag Pill */}
        <span
          className={`card-status-pill ${
            movie.status === 'CURRENTLY_RUNNING' ? 'status-running' : 'status-coming'
          }`}
        >
          {movie.status === 'CURRENTLY_RUNNING' ? 'Showing Now' : 'Coming Soon'}
        </span>

        {/* Quick view hover action button */}
        <div className="card-hover-action">
          <span className="play-icon-bubble">▶</span>
          <span className="hover-action-text">View Details & Showtimes</span>
        </div>
      </div>

      {/* Content Section Underneath the Image */}
      <div className="card-info-content">
        {/* Movie Title Underneath Image */}
        <h3 className="card-movie-title" title={movie.title}>
          {movie.title}
        </h3>

        {/* Underneath Movie Title: Rating and Star Favorite */}
        <div className="card-meta-row">
          <div className="card-meta-left">
            <span className="card-rating-badge">{movie.rating || 'NR'}</span>
            <span className="card-genre-pill">{movie.genre}</span>
          </div>

          {/* Interactive Star Favorite Button */}
          <button
            type="button"
            className={`card-star-btn ${favorited ? 'is-favorited' : ''}`}
            onClick={handleStarClick}
            aria-label={favorited ? `Remove ${movie.title} from favorites` : `Add ${movie.title} to favorites`}
            title={favorited ? 'Remove from favorites' : 'Add to favorites'}
          >
            <span className="star-symbol">★</span>
          </button>
        </div>
      </div>
    </article>
  );
}
