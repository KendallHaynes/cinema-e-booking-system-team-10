const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

async function request(path) {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: 'include' });
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

export async function reserveShowtimeSeats(showId, showSeatIds, ticketCount) {
  const response = await fetch(
    `${API_BASE_URL}/api/showtimes/${encodeURIComponent(showId)}/reserve`,
    {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ showSeatIds, ticketCount })
    }
  );
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.detail || data.message || `Request failed (HTTP ${response.status}).`);
  }
  return data;
}
