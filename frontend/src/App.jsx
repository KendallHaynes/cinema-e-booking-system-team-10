import { MovieProvider, useMovieContext } from './context/MovieContext';
import Navbar from './components/Navbar';
import FilterModal from './components/FilterModal';
import Toast from './components/Toast';
import HomePage from './pages/HomePage';
import MovieDetailPage from './pages/MovieDetailPage';
import BookingPage from './pages/BookingPage';
import RegistrationPage from './pages/RegistrationPage';
import LoginPage from './pages/LoginPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import AdminPortalPage from './pages/AdminPortalPage';
import EditProfilePage from './pages/EditProfilePage';
import './App.css';

function MainContent() {
  const {
    activePage,
    toast,
    navigateHome,
    navigateLogin,
    navigateRegister,
    navigateForgotPassword,
    completeLogin,
    logout,
    currentUser,
    resetToken
  } = useMovieContext();

  return (
    <div className="ces-app-layout">
      {activePage !== 'admin-portal' && <Navbar />}

      <main className="ces-main-content">
        {activePage === 'home' && <HomePage />}
        {activePage === 'movie-detail' && <MovieDetailPage />}
        {activePage === 'booking' && <BookingPage />}
        {activePage === 'register' && <RegistrationPage onBackToHome={navigateHome} />}
        {activePage === 'login' && (
          <LoginPage
            onLogin={completeLogin}
            onForgotPassword={navigateForgotPassword}
            onRegister={navigateRegister}
          />
        )}
        {activePage === 'forgot-password' && <ForgotPasswordPage onBackToLogin={navigateLogin} />}
        {activePage === 'profile' && (
          <EditProfilePage onBack={navigateHome} onSessionExpired={navigateLogin} />
        )}
        {activePage === 'reset-password' && (
          <ResetPasswordPage token={resetToken} onBackToLogin={navigateLogin} />
        )}
        {activePage === 'admin-portal' && (
          <AdminPortalPage user={currentUser} onLogout={logout} />
        )}
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