const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: 'include', ...options });
  const data = response.status === 204 ? [] : await response.json().catch(() => []);
  if (!response.ok) {
    throw new Error(data.detail || data.message || `Request failed (HTTP ${response.status}).`);
  }
  return data;
}

export function getShowtimes(movieId) {
  const query = movieId ? `?movieId=${encodeURIComponent(movieId)}` : '';
  return request(`/api/showtimes${query}`);
}

export function getShowroomList() {
  return request('/api/showrooms');
}

export function getShowtimeSeats(showId) {
  return request(`/api/showtimes/${encodeURIComponent(showId)}/seats`);
}

function sendBookingRequest(showId, action, method, body) {
  return request(`/api/showtimes/${encodeURIComponent(showId)}/${action}`, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
}

export function selectShowtimeSeats(showId, showSeatIds) {
  return sendBookingRequest(showId, 'selection', 'PUT', { showSeatIds });
}

export function reserveShowtimeSeats(showId, showSeatIds, ticketCount) {
  return sendBookingRequest(showId, 'reserve', 'POST', { showSeatIds, ticketCount });
}

export function confirmCheckoutEmail(showId, email) {
  return sendBookingRequest(showId, 'checkout-email', 'POST', { email });
}
