import { useEffect, useState } from 'react';
import { createMovie, createPromotion, scheduleShowtime } from '../api/authApi';
import { getAllMovies } from '../api/movieApi';
import { getShowroomList, getShowtimes } from '../api/showtimeApi';
import { useMovieContext } from '../context/MovieContext';

const adminSections = ['Manage Movies', 'Manage Promotions', 'Manage Users', 'Manage Showtimes'];
const initialMovie = {
  title: '',
  genre: '',
  rating: '',
  description: '',
  posterUrl: '',
  trailerUrl: '',
  status: 'CURRENTLY_RUNNING',
  director: '',
  cast: '',
  producer: ''
};

function localDateTimeMinimum() {
  const date = new Date(Date.now() - new Date().getTimezoneOffset() * 60000);
  return date.toISOString().slice(0, 16);
}

function localDateToday() {
  const date = new Date();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

const today = localDateToday();
const initialPromotion = {
  name: '',
  discountPercentage: '',
  startDate: today,
  endDate: today
};

function formatShowtime(showtime) {
  return new Date(showtime.dateTime).toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short'
  });
}

export default function AdminPortalPage({ user, onLogout }) {
  const [selectedSection, setSelectedSection] = useState('');
  const [movieForm, setMovieForm] = useState(initialMovie);
  const [promotionForm, setPromotionForm] = useState(initialPromotion);
  const [movies, setMovies] = useState([]);
  const [showrooms, setShowrooms] = useState([]);
  const [showtimes, setShowtimes] = useState([]);
  const [scheduleForm, setScheduleForm] = useState({ movieId: '', roomId: '', dateTime: '' });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const { refreshMovies } = useMovieContext();

  useEffect(() => {
    let ignore = false;
    Promise.all([getAllMovies(), getShowroomList(), getShowtimes()])
      .then(([movieResult, roomList, showList]) => {
        if (ignore) return;
        setMovies(movieResult.data);
        setShowrooms(roomList);
        setShowtimes(showList);
        setScheduleForm((current) => ({
          ...current,
          movieId: current.movieId || String(movieResult.data[0]?.movieId || ''),
          roomId: current.roomId || String(roomList[0]?.roomId || '')
        }));
      })
      .catch((requestError) => {
        if (!ignore) setError(requestError.message || 'Could not load movie and showroom data.');
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });
    return () => {
      ignore = true;
    };
  }, []);

  async function loadAdminData() {
    setLoading(true);
    setError('');
    try {
      const [movieResult, roomList, showList] = await Promise.all([
        getAllMovies(),
        getShowroomList(),
        getShowtimes()
      ]);
      setMovies(movieResult.data);
      setShowrooms(roomList);
      setShowtimes(showList);
    } catch (requestError) {
      setError(requestError.message || 'Could not load movie and showroom data.');
      throw requestError;
    } finally {
      setLoading(false);
    }
  }

  async function submitMovie(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    setSubmitting(true);
    try {
      await createMovie(movieForm);
      setMovieForm(initialMovie);
      setMessage('Movie added to the catalog.');
      try {
        await Promise.all([loadAdminData(), refreshMovies()]);
      } catch (refreshError) {
        setError(`Movie was saved, but the catalog could not be refreshed: ${refreshError.message}`);
      }
    } catch (requestError) {
      setError(requestError.message || 'Could not add the movie.');
    } finally {
      setSubmitting(false);
    }
  }

  async function submitShowtime(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    setSubmitting(true);
    try {
      await scheduleShowtime({
        movieId: Number(scheduleForm.movieId),
        roomId: Number(scheduleForm.roomId),
        dateTime: scheduleForm.dateTime
      });
      setScheduleForm((current) => ({ ...current, dateTime: '' }));
      setMessage('Showtime scheduled and its seat map is ready.');
      try {
        await loadAdminData();
      } catch (refreshError) {
        setError(`Showtime was saved, but the upcoming list could not be refreshed: ${refreshError.message}`);
      }
    } catch (requestError) {
      setError(requestError.message || 'Could not schedule the showtime.');
    } finally {
      setSubmitting(false);
    }
  }

  async function submitPromotion(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    setSubmitting(true);
    try {
      const result = await createPromotion({
        ...promotionForm,
        discountPercentage: Number(promotionForm.discountPercentage)
      });
      setPromotionForm(initialPromotion);
      setMessage(
        result.emailFailures
          ? `Promotion created. ${result.emailsSent} subscriber email(s) sent; ${result.emailFailures} failed. Check your SMTP configuration.`
          : `Promotion created. Email sent to ${result.emailsSent} subscribed customer(s).`
      );
    } catch (requestError) {
      setError(requestError.message || 'Could not create the promotion.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="admin-portal" aria-labelledby="admin-portal-heading">
      <header className="admin-portal-header">
        <div>
          <p className="registration-intro">Cinema E-Booking System</p>
          <h1 id="admin-portal-heading">Admin Portal</h1>
          <p>Signed in as {user?.name || user?.email}</p>
        </div>
        <button type="button" className="login-action-btn" onClick={onLogout}>Log out</button>
      </header>

      <h2>Admin menu</h2>
      <nav className="admin-menu" aria-label="Admin menu">
        {adminSections.map((section) => (
          <button
            key={section}
            type="button"
            className={`admin-menu-item ${selectedSection === section ? 'active' : ''}`}
            onClick={() => {
              setSelectedSection(section);
              setError('');
              setMessage('');
            }}
          >
            {section}
          </button>
        ))}
      </nav>

      <div className="admin-portal-panel">
        {error && <p className="registration-error" role="alert">{error}</p>}
        {message && <p className="registration-success" role="status">{message}</p>}

        {selectedSection === 'Manage Movies' && (
          <section aria-labelledby="admin-movie-heading">
            <h2 id="admin-movie-heading">Add a movie</h2>
            <p className="registration-intro">Fields marked * are required.</p>
            <form className="sprint3-admin-form" onSubmit={submitMovie}>
              <label>Title *<input required maxLength="255" value={movieForm.title} onChange={(event) => setMovieForm({ ...movieForm, title: event.target.value })} /></label>
              <label>Genre *<input required maxLength="100" value={movieForm.genre} onChange={(event) => setMovieForm({ ...movieForm, genre: event.target.value })} /></label>
              <label>Rating<input maxLength="20" placeholder="PG-13" value={movieForm.rating} onChange={(event) => setMovieForm({ ...movieForm, rating: event.target.value })} /></label>
              <label>Status *
                <select required value={movieForm.status} onChange={(event) => setMovieForm({ ...movieForm, status: event.target.value })}>
                  <option value="CURRENTLY_RUNNING">Currently running</option>
                  <option value="COMING_SOON">Coming soon</option>
                </select>
              </label>
              <label className="sprint3-full-width">Description *<textarea required rows="3" value={movieForm.description} onChange={(event) => setMovieForm({ ...movieForm, description: event.target.value })} /></label>
              <label>Poster URL<input type="url" maxLength="500" value={movieForm.posterUrl} onChange={(event) => setMovieForm({ ...movieForm, posterUrl: event.target.value })} /></label>
              <label>Trailer URL<input type="url" maxLength="500" value={movieForm.trailerUrl} onChange={(event) => setMovieForm({ ...movieForm, trailerUrl: event.target.value })} /></label>
              <label>Director<input maxLength="255" value={movieForm.director} onChange={(event) => setMovieForm({ ...movieForm, director: event.target.value })} /></label>
              <label>Producer<input maxLength="255" value={movieForm.producer} onChange={(event) => setMovieForm({ ...movieForm, producer: event.target.value })} /></label>
              <label className="sprint3-full-width">Cast<input value={movieForm.cast} onChange={(event) => setMovieForm({ ...movieForm, cast: event.target.value })} /></label>
              <button className="registration-submit sprint3-full-width" type="submit" disabled={submitting}>
                {submitting ? 'Saving…' : 'Add Movie'}
              </button>
            </form>
          </section>
        )}

        {selectedSection === 'Manage Showtimes' && (
          <section aria-labelledby="admin-showtime-heading">
            <h2 id="admin-showtime-heading">Schedule a movie</h2>
            <p className="registration-intro">The same showroom cannot host two movies at the same time.</p>
            <form className="sprint3-admin-form" onSubmit={submitShowtime}>
              <label>Movie *
                <select required value={scheduleForm.movieId} onChange={(event) => setScheduleForm({ ...scheduleForm, movieId: event.target.value })}>
                  <option value="">Choose a movie</option>
                  {movies.map((movie) => <option key={movie.movieId} value={movie.movieId}>{movie.title}</option>)}
                </select>
              </label>
              <label>Showroom *
                <select required value={scheduleForm.roomId} onChange={(event) => setScheduleForm({ ...scheduleForm, roomId: event.target.value })}>
                  <option value="">Choose a showroom</option>
                  {showrooms.map((room) => <option key={room.roomId} value={room.roomId}>{room.name} ({room.capacity} seats)</option>)}
                </select>
              </label>
              <label className="sprint3-full-width">Date and time *
                <input required type="datetime-local" min={localDateTimeMinimum()} value={scheduleForm.dateTime} onChange={(event) => setScheduleForm({ ...scheduleForm, dateTime: event.target.value })} />
              </label>
              <button className="registration-submit sprint3-full-width" type="submit" disabled={submitting || !movies.length || !showrooms.length}>
                {submitting ? 'Scheduling…' : 'Add Showtime'}
              </button>
            </form>
            <div className="sprint3-showtime-list">
              <h3>Upcoming showtimes</h3>
              {loading ? <p>Loading showtimes…</p> : showtimes.length === 0
                ? <p>No upcoming showtimes yet.</p>
                : showtimes.map((showtime) => (
                  <p key={showtime.showId}>
                    <strong>{showtime.movieTitle}</strong> — {formatShowtime(showtime)} · {showtime.roomName}
                  </p>
                ))}
            </div>
          </section>
        )}

        {selectedSection === 'Manage Promotions' && (
          <section aria-labelledby="admin-promotion-heading">
            <h2 id="admin-promotion-heading">Create a promotion</h2>
            <p className="registration-intro">Fields marked * are required. The promotion email is sent only to active customers who subscribed to promotion emails.</p>
            <form className="sprint3-admin-form" onSubmit={submitPromotion}>
              <label className="sprint3-full-width">Promotion name *<input required maxLength="100" value={promotionForm.name} onChange={(event) => setPromotionForm({ ...promotionForm, name: event.target.value })} /></label>
              <label>Discount percentage *<input required type="number" min="0.01" max="100" step="0.01" value={promotionForm.discountPercentage} onChange={(event) => setPromotionForm({ ...promotionForm, discountPercentage: event.target.value })} /></label>
              <label>Start date *<input required type="date" min={today} value={promotionForm.startDate} onChange={(event) => setPromotionForm({ ...promotionForm, startDate: event.target.value, endDate: promotionForm.endDate < event.target.value ? event.target.value : promotionForm.endDate })} /></label>
              <label>End date *<input required type="date" min={promotionForm.startDate || today} value={promotionForm.endDate} onChange={(event) => setPromotionForm({ ...promotionForm, endDate: event.target.value })} /></label>
              <button className="registration-submit sprint3-full-width" type="submit" disabled={submitting}>
                {submitting ? 'Creating and sending…' : 'Create Promotion and Email Subscribers'}
              </button>
            </form>
          </section>
        )}
        {selectedSection === 'Manage Users' && <p>User management is not required for this sprint.</p>}
        {!selectedSection && <p>Select Manage Movies or Manage Showtimes to continue.</p>}
      </div>
    </section>
  );
}
