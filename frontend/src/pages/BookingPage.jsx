import { useEffect, useMemo, useState } from 'react';
import { confirmCheckoutEmail, getShowtimeSeats, selectShowtimeSeats } from '../api/showtimeApi';
import { useMovieContext } from '../context/MovieContext';

const TICKET_TYPES = {
  adult: { label: 'Adult', ages: 'Ages 13–59', price: 13.5 },
  child: { label: 'Child', ages: 'Ages 2–12', price: 9.0 },
  senior: { label: 'Senior', ages: 'Ages 60+', price: 10.5 }
};
const EMPTY_SEATS = [];

function getTicketTotal(counts) {
  return Object.entries(counts).reduce(
    (total, [type, quantity]) => total + quantity * TICKET_TYPES[type].price,
    0
  );
}

function formatShowtime(showtime) {
  return new Date(showtime.dateTime).toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short'
  });
}

export default function BookingPage() {
  const {
    selectedMovie,
    selectedShowtime,
    ticketCounts,
    setTicketCounts,
    selectedSeats,
    setSelectedSeats,
    checkoutStep,
    setCheckoutStep,
    checkoutEmail,
    setCheckoutEmail,
    checkoutReserved,
    checkoutError,
    setCheckoutError,
    beginCheckout,
    currentUser,
    openMovieDetail,
    navigateHome
  } = useMovieContext();
  const [seatResult, setSeatResult] = useState({ showId: null, seats: [], error: '' });
  const [validationError, setValidationError] = useState('');
  const [reservingSeats, setReservingSeats] = useState(false);
  const selectedShowId = selectedShowtime?.showId;

  const seatsLoading = seatResult.showId !== selectedShowId;
  const seatsError = seatResult.showId === selectedShowId ? seatResult.error : '';
  const seats = seatResult.showId === selectedShowId ? seatResult.seats : EMPTY_SEATS;
  const ticketCount = useMemo(
    () => Object.values(ticketCounts).reduce((total, count) => total + count, 0),
    [ticketCounts]
  );
  const total = getTicketTotal(ticketCounts);
  const seatRows = useMemo(() => {
    const rows = new Map();
    seats.forEach((seat) => {
      const row = seat.seatLabel.slice(0, 1);
      rows.set(row, [...(rows.get(row) || []), seat]);
    });
    return [...rows.entries()];
  }, [seats]);

  useEffect(() => {
    if (!selectedShowId) return undefined;
    let ignore = false;
    const showId = selectedShowId;
    getShowtimeSeats(showId)
      .then((showSeats) => {
        if (!ignore) setSeatResult({ showId, seats: showSeats, error: '' });
      })
      .catch((error) => {
        if (!ignore) {
          setSeatResult({ showId, seats: [], error: error.message || 'Could not load this seat map.' });
        }
      });
    return () => {
      ignore = true;
    };
  }, [selectedShowId, checkoutStep]);

  if (!selectedMovie || !selectedShowtime) {
    return (
      <div className="empty-results-card">
        <h2>No Showtime Selected</h2>
        <button type="button" className="ces-btn btn-primary" onClick={navigateHome}>
          Back to Home
        </button>
      </div>
    );
  }

  function changeTicketCount(type, delta) {
    setTicketCounts((current) => ({
      ...current,
      [type]: Math.max(0, Math.min(8, current[type] + delta))
    }));
    setValidationError('');
  }

  async function toggleSeat(seat) {
    const next = selectedSeats.includes(seat.showSeatId)
      ? selectedSeats.filter((id) => id !== seat.showSeatId)
      : [...selectedSeats, seat.showSeatId];
    setReservingSeats(true);
    setValidationError('');
    setCheckoutError('');
    try {
      await selectShowtimeSeats(selectedShowId, next);
      setSelectedSeats(next);
    } catch (error) {
      setValidationError(error.message);
      try {
        const updated = await getShowtimeSeats(selectedShowId);
        setSeatResult({ showId: selectedShowId, seats: updated, error: '' });
      } catch (refreshError) {
        setValidationError(refreshError.message);
      }
    } finally {
      setReservingSeats(false);
    }
  }

  async function continueToPayment(event) {
    event.preventDefault();
    setValidationError('');
    setReservingSeats(true);
    try {
      await confirmCheckoutEmail(selectedShowId, checkoutEmail);
      setCheckoutStep('payment');
    } catch (error) {
      setValidationError(error.message);
    } finally {
      setReservingSeats(false);
    }
  }

  async function continueToCheckout() {
    if (ticketCount < 1) {
      setValidationError('Select at least one ticket before choosing seats.');
      return;
    }
    if (selectedSeats.length !== ticketCount) {
      setValidationError(`Select exactly ${ticketCount} seat${ticketCount === 1 ? '' : 's'} for your tickets.`);
      return;
    }
    if (selectedSeats.some((id) => !seats.some((seat) => seat.showSeatId === id && seat.available))) {
      setValidationError('A selected seat is no longer available. Review your selection.');
      return;
    }
    setValidationError('');
    setReservingSeats(true);
    try {
      await beginCheckout();
    } finally {
      setReservingSeats(false);
    }
  }

  const selectedSeatLabels = selectedSeats
    .map((id) => seats.find((seat) => seat.showSeatId === id)?.seatLabel)
    .filter(Boolean);

  return (
    <div className="booking-page-container">
      <div className="booking-top-nav">
        <button type="button" className="ces-back-btn" onClick={() => openMovieDetail(selectedMovie)}>
          <span className="back-arrow">←</span>
          <span>Back to Movie Details</span>
        </button>
      </div>

      <div className="booking-movie-banner">
        <div className="banner-poster-wrap">
          <img src={selectedMovie.posterUrl} alt={selectedMovie.title} className="banner-poster" />
        </div>
        <div className="banner-details">
          <div className="banner-top-badges">
            <span className="banner-rating">{selectedMovie.rating || 'NR'}</span>
            <span className="banner-genre">{selectedMovie.genre}</span>
          </div>
          <h1 className="banner-title">{selectedMovie.title}</h1>
          <div className="banner-showtime-chip">
            <span className="showtime-text">
              {formatShowtime(selectedShowtime)} · {selectedShowtime.roomName}
            </span>
          </div>
        </div>
      </div>

      {checkoutStep === 'seats' && (
        <div className="booking-layout-columns">
          <div className="booking-main-column">
            <section className="booking-section-card">
              <div className="booking-section-header">
                <span className="step-number">1</span>
                <div>
                  <h2 className="step-title">Select ticket quantities</h2>
                  <p className="step-subtitle">Choose the number of tickets in each age category.</p>
                </div>
              </div>
              <div className="ticket-categories-list">
                {Object.entries(TICKET_TYPES).map(([type, details]) => (
                  <div key={type} className="ticket-row-item">
                    <div className="ticket-info">
                      <h3 className="ticket-type-name">{details.label}</h3>
                      <p className="ticket-desc">{details.ages}</p>
                      <span className="ticket-unit-price">${details.price.toFixed(2)} each</span>
                    </div>
                    <div className="ticket-stepper-control">
                      <button type="button" className="stepper-btn" aria-label={`Decrease ${details.label} tickets`} disabled={ticketCounts[type] === 0} onClick={() => changeTicketCount(type, -1)}>-</button>
                      <span className="stepper-val">{ticketCounts[type]}</span>
                      <button type="button" className="stepper-btn" aria-label={`Increase ${details.label} tickets`} disabled={ticketCount >= 8} onClick={() => changeTicketCount(type, 1)}>+</button>
                    </div>
                    <div className="ticket-item-subtotal">${(ticketCounts[type] * details.price).toFixed(2)}</div>
                  </div>
                ))}
              </div>
              <p className="registration-intro">Tickets selected: {ticketCount} (maximum 8 per booking).</p>
            </section>

            <section className="booking-section-card">
              <div className="booking-section-header">
                <span className="step-number">2</span>
                <div>
                  <h2 className="step-title">Choose your seats</h2>
                  <p className="step-subtitle">Select one available seat for each ticket.</p>
                </div>
              </div>
              <div className="cinema-screen-area">
                <div className="screen-curve"></div>
                <span className="screen-text">Screen</span>
              </div>
              {seatsLoading && <p>Loading seat map…</p>}
              {seatsError && <p className="registration-error" role="alert">{seatsError}</p>}
              {checkoutError && <p className="registration-error" role="alert">{checkoutError}</p>}
              {!seatsLoading && !seatsError && seats.length === 0 && <p>No seat map is configured for this show.</p>}
              <div className="seats-grid-wrapper">
                <div className="seats-grid">
                  {seatRows.map(([row, rowSeats]) => (
                    <div className="seat-row" key={row}>
                      <span className="seat-row-label">{row}</span>
                      <div className="seat-row-items">
                        {rowSeats.map((seat) => {
                          const isSelected = selectedSeats.includes(seat.showSeatId);
                          const className = `seat-btn ${seat.available ? (isSelected ? 'seat-selected' : 'seat-available') : 'seat-occupied'}`;
                          return (
                            <button
                              key={seat.showSeatId}
                              type="button"
                              className={className}
                              disabled={reservingSeats || !seat.available || (!isSelected && selectedSeats.length >= ticketCount)}
                              onClick={() => toggleSeat(seat)}
                              title={`${seat.seatLabel}: ${seat.available ? (isSelected ? 'Selected' : 'Available') : 'Booked'}`}
                              aria-label={`Seat ${seat.seatLabel}${seat.available ? '' : ', booked'}`}
                              aria-pressed={isSelected}
                            >
                              <span className="seat-code">{seat.seatLabel}</span>
                            </button>
                          );
                        })}
                      </div>
                      <span className="seat-row-label">{row}</span>
                    </div>
                  ))}
                </div>
              </div>
              <div className="seats-legend-bar">
                <div className="legend-item"><span className="legend-sample available"></span><span>Available</span></div>
                <div className="legend-item"><span className="legend-sample selected"></span><span>Selected</span></div>
                <div className="legend-item"><span className="legend-sample occupied"></span><span>Booked</span></div>
              </div>
              <p className="registration-intro">Your session holds each selected seat while you sign in and review your order.</p>
            </section>
          </div>

          <aside className="booking-summary-column">
            <div className="order-summary-card">
              <h2 className="summary-title">Booking summary</h2>
              <div className="summary-movie-info">
                <h3 className="summary-movie-title">{selectedMovie.title}</h3>
                <p className="summary-time">{formatShowtime(selectedShowtime)}</p>
                <p className="summary-time">{selectedShowtime.roomName}</p>
              </div>
              <div className="summary-divider"></div>
              {Object.entries(TICKET_TYPES).map(([type, details]) => ticketCounts[type] > 0 && (
                <div className="summary-row" key={type}>
                  <span>{ticketCounts[type]} × {details.label} (${details.price.toFixed(2)})</span>
                  <span>${(ticketCounts[type] * details.price).toFixed(2)}</span>
                </div>
              ))}
              <div className="summary-row selected-seats-row">
                <span>Selected seats</span>
                <span>{selectedSeatLabels.length ? selectedSeatLabels.join(', ') : 'None'}</span>
              </div>
              <div className="summary-total-row">
                <span className="total-label">Total before tax</span>
                <span className="total-amount">${total.toFixed(2)}</span>
              </div>
              {validationError && <p className="registration-error" role="alert">{validationError}</p>}
              <button type="button" className="ces-btn btn-primary btn-block btn-checkout" disabled={seatsLoading || !!seatsError || reservingSeats || checkoutReserved} onClick={continueToCheckout}>
                {reservingSeats ? 'Reserving seats…' : checkoutReserved ? 'Seats Reserved' : 'Proceed to Checkout'}
              </button>
            </div>
          </aside>
        </div>
      )}

      {checkoutStep === 'summary' && (
        <section className="booking-section-card sprint3-checkout-card" aria-labelledby="checkout-summary-heading">
          <h2 id="checkout-summary-heading">Order summary</h2>
          {checkoutReserved && <p className="registration-success" role="status">Your selected seats are reserved for this showtime and are unavailable to other customers.</p>}
          <dl className="sprint3-order-details">
            <div><dt>Movie</dt><dd>{selectedMovie.title}</dd></div>
            <div><dt>Showtime</dt><dd>{formatShowtime(selectedShowtime)} · {selectedShowtime.roomName}</dd></div>
            <div><dt>Selected seats</dt><dd>{selectedSeatLabels.join(', ')}</dd></div>
            <div><dt>Tickets</dt><dd>{Object.entries(TICKET_TYPES).filter(([type]) => ticketCounts[type] > 0).map(([type, details]) => `${ticketCounts[type]} ${details.label}`).join(', ')}</dd></div>
            <div><dt>Price per ticket</dt><dd>{Object.entries(TICKET_TYPES).filter(([type]) => ticketCounts[type] > 0).map(([, details]) => `${details.label}: $${details.price.toFixed(2)}`).join(' · ')}</dd></div>
            <div className="sprint3-order-total"><dt>Total before tax</dt><dd>${total.toFixed(2)}</dd></div>
          </dl>
          <form onSubmit={continueToPayment}>
            {validationError && <p role="alert" className="registration-error">{validationError}</p>}
            <label className="sprint3-email-label">
              Confirmation email *
              <input type="email" required maxLength="255" value={checkoutEmail} onChange={(event) => setCheckoutEmail(event.target.value)} />
            </label>
            <p className="registration-intro">Confirm your account email or enter another address for this checkout.</p>
            <div className="sprint3-checkout-actions">
              {!checkoutReserved && <button type="button" className="ces-back-btn" onClick={() => setCheckoutStep('seats')}>Back to seats</button>}
              <button type="submit" disabled={reservingSeats} className="registration-submit">Continue to Payment</button>
            </div>
          </form>
          {!currentUser && <p className="registration-intro">Sign in is required before checkout can continue.</p>}
        </section>
      )}

      {checkoutStep === 'payment' && (
        <section className="booking-section-card sprint3-checkout-card" aria-labelledby="payment-heading">
          <h2 id="payment-heading">Payment Processing</h2>
          <p className="registration-intro">Demo payment page — no payment information is collected and no charge is processed.</p>
          <div className="sprint3-mock-payment" aria-label="Payment processing mockup">
            <div className="sprint3-mock-payment-heading">
              <span className="sprint3-mock-payment-icon" aria-hidden="true">▣</span>
              <div>
                <h3>Secure checkout</h3>
                <p>Payment gateway mockup</p>
              </div>
            </div>
            <label>Payment method
              <select disabled defaultValue="card">
                <option value="card">Credit or debit card</option>
              </select>
            </label>
            <label>Card number
              <input disabled value="••••  ••••  ••••  ••••" readOnly />
            </label>
            <div className="sprint3-mock-payment-fields">
              <label>Expiration date<input disabled value="MM / YY" readOnly /></label>
              <label>Security code<input disabled value="•••" readOnly /></label>
            </div>
            <button type="button" className="registration-submit" disabled>Payment processing unavailable in this demo</button>
          </div>
          <div className="sprint3-payment-summary">
            <p><strong>{selectedMovie.title}</strong> · {selectedSeatLabels.join(', ')}</p>
            <p>Confirmation email: {checkoutEmail}</p>
            <p>Total before tax: <strong>${total.toFixed(2)}</strong></p>
            <p>Seats are reserved for this showtime. No booking confirmation is issued from this mock page.</p>
          </div>
          <button type="button" className="ces-back-btn" onClick={() => setCheckoutStep('summary')}>Back to order summary</button>
        </section>
      )}
    </div>
  );
}
