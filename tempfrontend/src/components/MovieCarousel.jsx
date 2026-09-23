import { useRef } from 'react';
import MovieCard from './MovieCard';

export default function MovieCarousel({ title, subtitle, movies, icon }) {
  const scrollContainerRef = useRef(null);

  const handleScroll = (direction) => {
    if (scrollContainerRef.current) {
      const scrollAmount = 480; // Scroll distance in pixels
      const targetScroll =
        direction === 'left'
          ? scrollContainerRef.current.scrollLeft - scrollAmount
          : scrollContainerRef.current.scrollLeft + scrollAmount;

      scrollContainerRef.current.scrollTo({
        left: targetScroll,
        behavior: 'smooth'
      });
    }
  };

  if (!movies || movies.length === 0) {
    return null;
  }

  return (
    <section className="ces-carousel-section">
      {/* Carousel Header with Title and Scroll Arrows */}
      <div className="carousel-header">
        <div className="carousel-title-group">
          {icon && <span className="carousel-icon">{icon}</span>}
          <div>
            <h2 className="carousel-title">{title}</h2>
            {subtitle && <p className="carousel-subtitle">{subtitle}</p>}
          </div>
          <span className="carousel-count-badge">{movies.length} Movies</span>
        </div>

        {/* Horizontal Scroll Navigation Arrows */}
        <div className="carousel-arrow-controls">
          <button
            type="button"
            className="carousel-nav-btn"
            onClick={() => handleScroll('left')}
            aria-label={`Scroll ${title} left`}
            title="Scroll Left"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
          </button>
          <button
            type="button"
            className="carousel-nav-btn"
            onClick={() => handleScroll('right')}
            aria-label={`Scroll ${title} right`}
            title="Scroll Right"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
          </button>
        </div>
      </div>

      {/* Horizontal Scroll Track */}
      <div className="carousel-scroll-track" ref={scrollContainerRef}>
        {movies.map((movie) => (
          <div key={movie.movieId} className="carousel-card-item">
            <MovieCard movie={movie} />
          </div>
        ))}
      </div>
    </section>
  );
}
