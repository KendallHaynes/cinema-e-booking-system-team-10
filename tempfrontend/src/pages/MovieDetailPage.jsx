import { useState } from 'react';
import { useMovieContext } from '../context/MovieContext';

const HARDCODED_SHOWTIMES = [
  { time: '2:00 PM', auditorium: 'Screen 1 (Standard 4K)', format: 'Digital' },
  { time: '5:00 PM', auditorium: 'Screen 2 (Dolby Cinema)', format: 'Dolby Atmos' },
  { time: '8:00 PM', auditorium: 'Screen 3 (IMAX Laser)', format: 'IMAX' },
  { time: '10:15 PM', auditorium: 'Screen 1 (Late Night)', format: 'Digital' }
];

export default function MovieDetailPage() {
  const { selectedMovie, navigateHome, openBooking, isFavorite, toggleFavorite } = useMovieContext();
  const [imgError, setImgError] = useState(false);

  if (!selectedMovie) {
    return (
      <div className="empty-results-card">
        <h2>No Movie Selected</h2>
        <button type="button" className="ces-btn btn-primary" onClick={navigateHome}>
          Back to Movies
        </button>
      </div>
    );
  }

  const favorited = isFavorite(selectedMovie.movieId);
  const isCurrentlyRunning = selectedMovie.status === 'CURRENTLY_RUNNING';

  const posterSrc = imgError || !selectedMovie.posterUrl
    ? 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&auto=format&fit=crop&q=80'
    : selectedMovie.posterUrl;

  return (
    <div className="movie-detail-container">
      {/* Top Navigation Row */}
      <div className="detail-top-nav">
        <button type="button" className="ces-back-btn" onClick={navigateHome}>
          <span className="back-arrow">←</span>
          <span>Back to Movies</span>
        </button>

        <span className="detail-status-pill">
          {isCurrentlyRunning ? 'Now In Theaters' : 'Coming Soon'}
        </span>
      </div>

      {/* TOP SECTION: Movie Title, Rating, Star, Description, Cast */}
      <header className="detail-header-card">
        <div className="detail-poster-column">
          <img
            src={posterSrc}
            alt={selectedMovie.title}
            className="detail-poster-img"
            onError={() => setImgError(true)}
          />
        </div>

        <div className="detail-info-column">
          <div className="detail-title-row">
            <h1 className="detail-movie-title">{selectedMovie.title}</h1>

            {/* Interactive Star Favorite Button */}
            <button
              type="button"
              className={`detail-star-btn ${favorited ? 'is-favorited' : ''}`}
              onClick={() => toggleFavorite(selectedMovie.movieId, selectedMovie.title)}
              title={favorited ? 'Remove from favorites' : 'Add to favorites'}
            >
              <span className="star-icon">★</span>
              <span className="star-label">{favorited ? 'Favorited' : 'Favorite'}</span>
            </button>
          </div>

          {/* Badges: Rating, Genre, Status */}
          <div className="detail-badges-row">
            <span className="detail-rating-badge">{selectedMovie.rating || 'NR'}</span>
            <span className="detail-genre-pill">{selectedMovie.genre}</span>
            <span className="detail-duration-pill">2h 35m</span>
            <span className="detail-hd-badge">4K Ultra HD</span>
          </div>

          {/* Brief Movie Description */}
          <div className="detail-description-section">
            <h3 className="section-label">Synopsis</h3>
            <p className="detail-description-text">{selectedMovie.description}</p>
          </div>

          {/* Crew & Credits */}
          <div className="detail-credits-grid">
            {selectedMovie.director && (
              <div className="credit-item">
                <span className="credit-label">Director</span>
                <span className="credit-val">{selectedMovie.director}</span>
              </div>
            )}
            {selectedMovie.cast && (
              <div className="credit-item">
                <span className="credit-label">Cast</span>
                <span className="credit-val">{selectedMovie.cast}</span>
              </div>
            )}
            {selectedMovie.producer && (
              <div className="credit-item">
                <span className="credit-label">Producer</span>
                <span className="credit-val">{selectedMovie.producer}</span>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* CENTER SECTION: Embedded Playable Video Trailer */}
      <section className="detail-trailer-section">
        <div className="section-title-bar">
          <span className="section-icon">▶</span>
          <h2 className="section-title">Official Trailer</h2>
        </div>

        <div className="trailer-embed-wrapper">
          {selectedMovie.trailerUrl ? (
            <iframe
              className="trailer-iframe"
              src={selectedMovie.trailerUrl}
              title={`${selectedMovie.title} Trailer`}
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              allowFullScreen
            ></iframe>
          ) : (
            <div className="no-trailer-placeholder">
              <p>No video trailer available for this title.</p>
            </div>
          )}
        </div>
      </section>

      {/* BOTTOM SECTION: Available Showtimes (e.g. 2:00 PM, 5:00 PM, 8:00 PM) */}
      <section className="detail-showtimes-section">
        <div className="section-title-bar">
          <span className="section-icon">🎟</span>
          <div>
            <h2 className="section-title">Available Showtimes</h2>
            <p className="section-subtitle">
              {isCurrentlyRunning
                ? 'Select a showtime to proceed to seat selection & booking'
                : 'Preview showtimes for early booking'}
            </p>
          </div>
        </div>

        <div className="showtimes-grid">
          {HARDCODED_SHOWTIMES.map((slot) => (
            <div
              key={slot.time}
              className="showtime-card"
              onClick={() => openBooking(selectedMovie, slot.time)}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => {
                if (e.key === 'Enter') openBooking(selectedMovie, slot.time);
              }}
            >
              <div className="showtime-top">
                <span className="showtime-format-badge">{slot.format}</span>
                <span className="showtime-auditorium">{slot.auditorium}</span>
              </div>
              <div className="showtime-time">{slot.time}</div>
              <button
                type="button"
                className="ces-btn btn-primary btn-sm showtime-book-btn"
                onClick={(e) => {
                  e.stopPropagation();
                  openBooking(selectedMovie, slot.time);
                }}
              >
                Select Seats →
              </button>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
