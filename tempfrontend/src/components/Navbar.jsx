import { useMovieContext } from '../context/MovieContext';

export default function Navbar() {
  const {
    searchQuery,
    handleSearch,
    setFilterModalOpen,
    selectedGenre,
    selectedDate,
    favorites,
    showFavoritesOnly,
    setShowFavoritesOnly,
    navigateHome,
    isLiveBackend
  } = useMovieContext();

  const hasActiveFilters = selectedGenre !== 'ALL' || selectedDate !== 'ALL';

  return (
    <header className="ces-navbar-wrapper">
      <div className="ces-navbar">
        {/* Top Left: Login Button (Placeholder per requirements, does not do anything yet) */}
        <div className="navbar-left">
          <button
            type="button"
            className="login-action-btn"
            aria-label="User Login"
          >
            <span className="login-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </span>
            <span className="login-label">Login</span>
          </button>

          {/* Cinema Brand Logo / Title */}
          <button type="button" className="brand-logo" onClick={navigateHome}>
            <span className="brand-accent">CES</span>
            <span className="brand-sub">CINEMAS</span>
          </button>
        </div>

        {/* Top Center: Search Bar & Filter Button right next to it */}
        <div className="navbar-center">
          <div className="search-filter-combo">
            <div className="search-bar-container">
              <span className="search-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
              </span>
              <input
                type="text"
                className="search-input"
                placeholder="Search movies by title..."
                value={searchQuery}
                onChange={(e) => handleSearch(e.target.value)}
              />
              {searchQuery && (
                <button
                  type="button"
                  className="search-clear-btn"
                  onClick={() => handleSearch('')}
                  aria-label="Clear search"
                >
                  ✕
                </button>
              )}
            </div>

            {/* Filter button next to search bar */}
            <button
              type="button"
              className={`filter-btn-trigger ${hasActiveFilters ? 'has-active-filter' : ''}`}
              onClick={() => setFilterModalOpen(true)}
              title="Filter by Genre or Showtime"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="4" y1="21" x2="4" y2="14"></line>
                <line x1="4" y1="10" x2="4" y2="3"></line>
                <line x1="12" y1="21" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12" y2="3"></line>
                <line x1="20" y1="21" x2="20" y2="16"></line>
                <line x1="20" y1="12" x2="20" y2="3"></line>
                <line x1="1" y1="14" x2="7" y2="14"></line>
                <line x1="9" y1="8" x2="15" y2="8"></line>
                <line x1="17" y1="16" x2="23" y2="16"></line>
              </svg>
              <span className="filter-label">Filter</span>
              {hasActiveFilters && <span className="active-dot" />}
            </button>
          </div>
        </div>

        {/* Top Right: Favorites & Status Indicator */}
        <div className="navbar-right">
          <button
            type="button"
            className={`favorites-btn-nav ${showFavoritesOnly ? 'active' : ''}`}
            onClick={() => setShowFavoritesOnly(!showFavoritesOnly)}
            title="View Favorited Movies"
          >
            <span className="star-icon">★</span>
            <span className="favorites-text">Favorites</span>
            <span className="favorites-badge">{favorites.length}</span>
          </button>

          <div
            className={`db-status-pill ${isLiveBackend ? 'live-db' : 'demo-db'}`}
            title={isLiveBackend ? 'Connected to live Spring Boot DB' : 'Using Demo DB Seeds (Spring Boot offline)'}
          >
            <span className="status-dot"></span>
            <span className="status-label">{isLiveBackend ? 'Live DB' : 'Demo DB'}</span>
          </div>
        </div>
      </div>
    </header>
  );
}
