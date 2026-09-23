import { useMovieContext } from '../context/MovieContext';

// Ticket pricing definitions
const TICKET_TYPES = {
  adult: { name: 'Adult', price: 13.5, desc: 'Ages 13 - 59', defaultQty: 2 },
  child: { name: 'Child', price: 9.0, desc: 'Ages 2 - 12', defaultQty: 0 },
  senior: { name: 'Senior', price: 10.5, desc: 'Ages 60+', defaultQty: 0 }
};

// Preset occupied seats for prototype realism
const DEFAULT_OCCUPIED = ['B3', 'B4', 'C5', 'C6', 'D1', 'D2', 'E7', 'E8'];
const DEFAULT_SELECTED = ['C3', 'C4'];

export default function BookingPage() {
  const {
    selectedMovie,
    selectedShowtime,
    openMovieDetail,
    navigateHome
  } = useMovieContext();

  // Rows A to F, seats 1 to 8
  const rows = ['A', 'B', 'C', 'D', 'E', 'F'];
  const seatNumbers = [1, 2, 3, 4, 5, 6, 7, 8];

  // Static prototype totals per requirement (buttons do not alter totals yet)
  const prototypeSubtotal = 27.0;
  const prototypeFee = 3.0;
  const prototypeGrandTotal = 30.0;

  if (!selectedMovie) {
    return (
      <div className="empty-results-card">
        <h2>No Showtime Selected</h2>
        <button type="button" className="ces-btn btn-primary" onClick={navigateHome}>
          Back to Home
        </button>
      </div>
    );
  }

  return (
    <div className="booking-page-container">
      {/* Top Breadcrumb */}
      <div className="booking-top-nav">
        <button
          type="button"
          className="ces-back-btn"
          onClick={() => openMovieDetail(selectedMovie)}
        >
          <span className="back-arrow">←</span>
          <span>Back to Movie Details</span>
        </button>
      </div>

      {/* Selected Movie & Showtime Bar */}
      <div className="booking-movie-banner">
        <div className="banner-poster-wrap">
          <img
            src={selectedMovie.posterUrl}
            alt={selectedMovie.title}
            className="banner-poster"
          />
        </div>
        <div className="banner-details">
          <div className="banner-top-badges">
            <span className="banner-rating">{selectedMovie.rating || 'PG-13'}</span>
            <span className="banner-genre">{selectedMovie.genre}</span>
          </div>
          <h1 className="banner-title">{selectedMovie.title}</h1>
          <div className="banner-showtime-chip">
            <span className="clock-icon">🕒</span>
            <span className="showtime-text">
              Selected Showtime: <strong>{selectedShowtime || '5:00 PM'}</strong> (Today) • Auditorium 2
            </span>
          </div>
        </div>
      </div>

      <div className="booking-layout-columns">
        {/* Left Column: Tickets & Seating Chart */}
        <div className="booking-main-column">
          {/* STEP 1: Ticket Quantities (Prototype Display) */}
          <section className="booking-section-card">
            <div className="booking-section-header">
              <span className="step-number">1</span>
              <div>
                <h2 className="step-title">Select Ticket Quantities</h2>
                <p className="step-subtitle">Ticket counts for each age category</p>
              </div>
            </div>

            <div className="ticket-categories-list">
              {Object.entries(TICKET_TYPES).map(([typeKey, info]) => {
                const qty = info.defaultQty;
                const cost = (qty * info.price).toFixed(2);

                return (
                  <div key={typeKey} className="ticket-row-item">
                    <div className="ticket-info">
                      <h4 className="ticket-type-name">{info.name}</h4>
                      <p className="ticket-desc">{info.desc}</p>
                      <span className="ticket-unit-price">${info.price.toFixed(2)} each</span>
                    </div>

                    <div className="ticket-stepper-control">
                      <button
                        type="button"
                        className="stepper-btn"
                        aria-label={`Decrease ${info.name} tickets`}
                      >
                        -
                      </button>
                      <span className="stepper-val">{qty}</span>
                      <button
                        type="button"
                        className="stepper-btn"
                        aria-label={`Increase ${info.name} tickets`}
                      >
                        +
                      </button>
                    </div>

                    <div className="ticket-item-subtotal">
                      ${cost}
                    </div>
                  </div>
                );
              })}
            </div>
          </section>

          {/* STEP 2: Theater Seating Layout Visualization (Seat numbers only, no emojis) */}
          <section className="booking-section-card">
            <div className="booking-section-header">
              <span className="step-number">2</span>
              <div>
                <h2 className="step-title">Theater Seating Layout</h2>
                <p className="step-subtitle">
                  Auditorium seating layout visualization
                </p>
              </div>
            </div>

            {/* Visual Cinema Screen Arc */}
            <div className="cinema-screen-area">
              <div className="screen-curve"></div>
              <span className="screen-text">CINEMA SCREEN</span>
            </div>

            {/* Seating Grid */}
            <div className="seats-grid-wrapper">
              <div className="seats-grid">
                {rows.map((row) => (
                  <div key={row} className="seat-row">
                    <span className="seat-row-label">{row}</span>
                    <div className="seat-row-items">
                      {seatNumbers.map((num) => {
                        const seatId = `${row}${num}`;
                        const isOccupied = DEFAULT_OCCUPIED.includes(seatId);
                        const isSelected = DEFAULT_SELECTED.includes(seatId);

                        let seatClass = 'seat-btn';
                        if (isOccupied) seatClass += ' seat-occupied';
                        else if (isSelected) seatClass += ' seat-selected';
                        else seatClass += ' seat-available';

                        return (
                          <button
                            key={seatId}
                            type="button"
                            className={seatClass}
                            disabled={isOccupied}
                            title={`Seat ${seatId} ${
                              isOccupied ? '(Occupied)' : isSelected ? '(Selected)' : '(Available)'
                            }`}
                            aria-label={`Seat ${seatId}`}
                          >
                            <span className="seat-code">{seatId}</span>
                          </button>
                        );
                      })}
                    </div>
                    <span className="seat-row-label">{row}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Seat Map Legend */}
            <div className="seats-legend-bar">
              <div className="legend-item">
                <span className="legend-sample available"></span>
                <span>Available</span>
              </div>
              <div className="legend-item">
                <span className="legend-sample selected"></span>
                <span>Selected</span>
              </div>
              <div className="legend-item">
                <span className="legend-sample occupied"></span>
                <span>Occupied</span>
              </div>
            </div>
          </section>
        </div>

        {/* Right Column: Order Summary */}
        <aside className="booking-summary-column">
          <div className="order-summary-card">
            <h3 className="summary-title">Booking Summary</h3>

            <div className="summary-movie-info">
              <h4 className="summary-movie-title">{selectedMovie.title}</h4>
              <p className="summary-movie-meta">
                {selectedMovie.rating || 'PG-13'} • {selectedMovie.genre}
              </p>
              <p className="summary-time">
                Showtime: <strong>{selectedShowtime || '5:00 PM'}</strong>
              </p>
            </div>

            <div className="summary-divider"></div>

            {/* Tickets breakdown */}
            <div className="summary-breakdown">
              <div className="summary-row">
                <span>Tickets (2)</span>
                <span>${prototypeSubtotal.toFixed(2)}</span>
              </div>
              <div className="summary-sub-row">
                <span>• 2x Adult</span>
                <span>$27.00</span>
              </div>

              <div className="summary-row">
                <span>Booking & Convenience Fee</span>
                <span>${prototypeFee.toFixed(2)}</span>
              </div>

              <div className="summary-row selected-seats-row">
                <span>Selected Seats</span>
                <span className="seats-badge-list">
                  {DEFAULT_SELECTED.join(', ')}
                </span>
              </div>
            </div>

            <div className="summary-divider"></div>

            <div className="summary-total-row">
              <span className="total-label">Estimated Total</span>
              <span className="total-amount">${prototypeGrandTotal.toFixed(2)}</span>
            </div>

            <button
              type="button"
              className="ces-btn btn-primary btn-block btn-checkout"
            >
              Confirm Reservation
            </button>
          </div>
        </aside>
      </div>
    </div>
  );
}
