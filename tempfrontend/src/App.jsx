import { MovieProvider, useMovieContext } from './context/MovieContext';
import Navbar from './components/Navbar';
import FilterModal from './components/FilterModal';
import Toast from './components/Toast';
import HomePage from './pages/HomePage';
import MovieDetailPage from './pages/MovieDetailPage';
import BookingPage from './pages/BookingPage';
import './App.css';

function MainContent() {
  const { activePage, toast } = useMovieContext();

  return (
    <div className="ces-app-layout">
      {/* Top Navbar */}
      <Navbar />

      {/* Main View Router */}
      <main className="ces-main-content">
        {activePage === 'home' && <HomePage />}
        {activePage === 'movie-detail' && <MovieDetailPage />}
        {activePage === 'booking' && <BookingPage />}
      </main>

      {/* Modals & Overlays */}
      <FilterModal />
      <Toast toast={toast} />

      {/* Footer */}
      <footer className="ces-footer">
        <div className="footer-inner">
          <div className="footer-brand">
            <span className="footer-brand-title">CES CINEMAS</span>
            <p className="footer-brand-desc">
              Cinema E-Booking System
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default function App() {
  return (
    <MovieProvider>
      <MainContent />
    </MovieProvider>
  );
}