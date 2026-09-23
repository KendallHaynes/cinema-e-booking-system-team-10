import { useMovieContext } from '../context/MovieContext';

const GENRES = [
  'ALL',
  'Action',
  'Science Fiction',
  'Animation',
  'Drama',
  'Comedy',
  'Fantasy'
];

const SHOW_DATES = [
  { id: 'ALL', label: 'All Dates' },
  { id: 'TODAY', label: 'Today (Sep 23)' },
  { id: 'TOMORROW', label: 'Tomorrow (Sep 24)' },
  { id: 'WEEKEND', label: 'This Weekend' }
];

export default function FilterModal() {
  const {
    filterModalOpen,
    setFilterModalOpen,
    selectedGenre,
    handleSelectGenre,
    selectedDate,
    handleSelectDate,
    clearFilters,
    showNotification
  } = useMovieContext();

  if (!filterModalOpen) return null;

  const handleApply = () => {
    setFilterModalOpen(false);
    showNotification(
      `Filters updated: Genre [${selectedGenre === 'ALL' ? 'All' : selectedGenre}], Date [${
        SHOW_DATES.find((d) => d.id === selectedDate)?.label || 'All'
      }]`,
      'info'
    );
  };

  const handleReset = () => {
    clearFilters();
    setFilterModalOpen(false);
  };

  return (
    <div className="modal-backdrop" onClick={() => setFilterModalOpen(false)}>
      <div className="modal-card filter-modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title-wrap">
            <span className="modal-badge">Browse Options</span>
            <h2 className="modal-title">Filter Movies</h2>
          </div>
          <button
            className="modal-close-btn"
            onClick={() => setFilterModalOpen(false)}
            aria-label="Close filters"
          >
            ✕
          </button>
        </div>

        <div className="filter-content">
          {/* Section 1: Genre Filter (Active Backend DB integration) */}
          <div className="filter-section">
            <div className="filter-section-header">
              <label className="filter-section-title">Filter by Genre</label>
              <span className="filter-status-tag active-tag">Live DB Filter</span>
            </div>
            <p className="filter-hint">Select a genre to filter dynamically from the database.</p>
            <div className="filter-chip-grid">
              {GENRES.map((genre) => {
                const isSelected = selectedGenre === genre;
                return (
                  <button
                    key={genre}
                    type="button"
                    className={`filter-chip ${isSelected ? 'active' : ''}`}
                    onClick={() => handleSelectGenre(genre)}
                  >
                    {genre === 'ALL' ? 'All Genres' : genre}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Section 2: Show Date / Showtime Filter */}
          <div className="filter-section">
            <div className="filter-section-header">
              <label className="filter-section-title">Filter by Show Date</label>
              <span className="filter-status-tag ui-tag">Date Filter</span>
            </div>
            <p className="filter-hint">
              Select date availability to filter showings.
            </p>
            <div className="filter-chip-grid">
              {SHOW_DATES.map((item) => {
                const isSelected = selectedDate === item.id;
                return (
                  <button
                    key={item.id}
                    type="button"
                    className={`filter-chip ${isSelected ? 'active' : ''}`}
                    onClick={() => handleSelectDate(item.id)}
                  >
                    {item.label}
                  </button>
                );
              })}
            </div>
          </div>
        </div>

        <div className="filter-actions">
          <button type="button" className="ces-btn btn-secondary" onClick={handleReset}>
            Reset Filters
          </button>
          <button type="button" className="ces-btn btn-primary" onClick={handleApply}>
            Apply & View Movies
          </button>
        </div>
      </div>
    </div>
  );
}
