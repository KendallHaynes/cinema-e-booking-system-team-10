import { useMovieContext } from '../context/MovieContext';
import MovieCarousel from '../components/MovieCarousel';
import MovieCard from '../components/MovieCard';

export default function HomePage() {
  const {
    movies,
    allMovies,
    loading,
    searchQuery,
    selectedGenre,
    showFavoritesOnly,
    favorites,
    clearFilters
  } = useMovieContext();

  const isFiltering =
    (searchQuery && searchQuery.trim().length > 0) ||
    selectedGenre !== 'ALL' ||
    showFavoritesOnly;

  // Determine movies to display when filtering / favorites mode
  let displayMovies = movies;
  if (showFavoritesOnly) {
    displayMovies = allMovies.filter((m) => favorites.includes(m.movieId));
  }

  // Partition all movies for the standard carousels
  const currentlyRunningMovies = allMovies.filter(
    (m) => m.status === 'CURRENTLY_RUNNING'
  );
  const comingSoonMovies = allMovies.filter((m) => m.status === 'COMING_SOON');

  if (loading) {
    return (
      <div className="loading-state-container">
        <div className="spinner"></div>
        <p className="loading-text">Loading cinema schedule...</p>
      </div>
    );
  }

  return (
    <div className="ces-home-page">
      {/* Search / Filter Active State */}
      {isFiltering ? (
        <section className="search-results-section">
          <div className="results-header-bar">
            <div>
              <span className="results-badge">
                {showFavoritesOnly ? '⭐ Saved Favorites' : 'Filtered Results'}
              </span>
              <h1 className="results-title">
                {showFavoritesOnly
                  ? 'Your Favorited Movies'
                  : searchQuery
                  ? `Search: "${searchQuery}"`
                  : `Genre: ${selectedGenre}`}
              </h1>
              <p className="results-subtitle">
                Found {displayMovies.length}{' '}
                {displayMovies.length === 1 ? 'movie' : 'movies'} matching your selection.
              </p>
            </div>
            <button
              type="button"
              className="ces-btn btn-secondary"
              onClick={clearFilters}
            >
              ✕ Clear All Filters
            </button>
          </div>

          {/* If no movies match criteria */}
          {displayMovies.length === 0 ? (
            <div className="empty-results-card">
              <div className="empty-icon">🎬</div>
              <h2 className="empty-title">No Movies Found</h2>
              <p className="empty-desc">
                {showFavoritesOnly
                  ? "You haven't added any movies to your favorites yet. Click the star icon on any movie to favorite it!"
                  : `We couldn't find any movies matching your search criteria. Please try a different title or reset your genre filters.`}
              </p>
              <button
                type="button"
                className="ces-btn btn-primary"
                onClick={clearFilters}
              >
                Reset & View All Movies
              </button>
            </div>
          ) : (
            <div className="results-grid">
              {displayMovies.map((movie) => (
                <MovieCard key={movie.movieId} movie={movie} />
              ))}
            </div>
          )}
        </section>
      ) : (
        /* Default Home View consisting only of Currently Showing and Coming Soon */
        <main className="carousels-container">
          {/* Currently Showing Carousel with Horizontal Scroll Arrows */}
          <MovieCarousel
            title="Currently Showing"
            subtitle="In Theaters Today • Experience in Laser IMAX & Dolby Atmos"
            movies={currentlyRunningMovies}
            icon="🎬"
          />

          {/* Coming Soon Carousel with Horizontal Scroll Arrows */}
          <MovieCarousel
            title="Coming Soon"
            subtitle="Upcoming Releases • Add to your favorites to receive alerts"
            movies={comingSoonMovies}
            icon="✨"
          />
        </main>
      )}
    </div>
  );
}
