import { useMovieContext } from '../context/MovieContext';

export default function Navbar() {
  const {
    searchQuery,
    handleSearch,
    setFilterModalOpen,
    navigateRegister,
    navigateLogin,
    navigateProfile,
    currentUser,
    logout,
    selectedGenre,
    selectedDate,
    favorites,
    showFavoritesOnly,
    setShowFavoritesOnly
  } = useMovieContext();

  const hasActiveFilters = selectedGenre !== 'ALL' || selectedDate !== 'ALL';

  return (
    <header className="ces-navbar-wrapper">
      <div className="ces-navbar">
        {/* Account navigation */}
        <div className="navbar-left">
          {currentUser ? (
            <>
              <button type="button" className="login-action-btn" onClick={navigateProfile}>
                <span className="login-label">My Profile</span>
              </button>
              <button type="button" className="login-action-btn" onClick={logout}>
                <span className="login-label">Log out</span>
              </button>
            </>
          ) : (
            <button type="button" className="login-action-btn" onClick={navigateLogin}>
              <span className="login-label">Login</span>
            </button>
          )}
          {!currentUser && (
            <button
              type="button"
              className="login-action-btn"
              onClick={navigateRegister}
              aria-label="Create an account"
            >
              <span className="login-label">Register</span>
            </button>
          )}

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
            <span className="favorites-text">Favorites</span>
            <span className="favorites-badge">{favorites.length}</span>
          </button>

        </div>
      </div>
    </header>
  );
}
