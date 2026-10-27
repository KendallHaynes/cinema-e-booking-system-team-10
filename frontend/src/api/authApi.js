const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    credentials: 'include',
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers
    }
  });

  const data = response.status === 204 ? {} : await response.json().catch(() => ({}));
  if (!response.ok) {
    if (response.status === 409) {
      throw new Error('An account with this email already exists. Try signing in or use a different email address.');
    }
    throw new Error(data.detail || data.message || `The request could not be completed (HTTP ${response.status}).`);
  }
  return data;
}

function post(path, body) {
  return request(path, { method: 'POST', body: JSON.stringify(body) });
}

export function registerAccount(details) {
  return post('/api/auth/register', details);
}

export function verifyEmail(token) {
  return post(`/api/auth/verify-email?token=${encodeURIComponent(token)}`, {});
}

export function loginAccount(credentials) {
  return post('/api/auth/login', credentials);
}

export function requestPasswordReset(email) {
  return post('/api/auth/forgot-password', { email });
}

export function resetPassword(details) {
  return post('/api/auth/reset-password', details);
}

export function getCurrentUser() {
  return request('/api/auth/me');
}

export function verifyAdminSession() {
  return request('/api/admin/session');
}

export function logoutAccount() {
  return post('/api/auth/logout', {});
}

export function getProfile() {
  return request('/api/profile');
}

export function saveProfile(details) {
  return request('/api/profile', { method: 'PUT', body: JSON.stringify(details) });
}

export function addPaymentCard(details) {
  return post('/api/profile/cards', details);
}

export function removePaymentCard(cardId) {
  return request(`/api/profile/cards/${cardId}`, { method: 'DELETE' });
}

export function changePassword(details) {
  return post('/api/profile/password', details);
}

export function getFavoriteMovies() {
  return request('/api/favorites');
}

export function addFavoriteMovie(movieId) {
  return post(`/api/favorites/${movieId}`, {});
}

export function removeFavoriteMovie(movieId) {
  return request(`/api/favorites/${movieId}`, { method: 'DELETE' });
}
