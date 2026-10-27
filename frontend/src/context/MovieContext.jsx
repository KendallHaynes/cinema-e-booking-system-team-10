/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import {
  getAllMovies,
  searchMoviesByTitle,
  filterMoviesByGenre
} from '../api/movieApi';
import {
  addFavoriteMovie,
  getCurrentUser,
  getFavoriteMovies,
  logoutAccount,
  removeFavoriteMovie,
  verifyAdminSession
} from '../api/authApi';

const MovieContext = createContext();

export function MovieProvider({ children }) {
  const [allMovies, setAllMovies] = useState([]);
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isLiveBackend, setIsLiveBackend] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedGenre, setSelectedGenre] = useState('ALL');
  const [selectedDate, setSelectedDate] = useState('ALL');
  const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);

  // Navigation / views
  const [activePage, setActivePage] = useState(() =>
    new URLSearchParams(window.location.search).has('verify')
      ? 'register'
      : new URLSearchParams(window.location.search).has('reset')
        ? 'reset-password'
        : 'home'
  );
  const [resetToken] = useState(() => new URLSearchParams(window.location.search).get('reset'));
  const [currentUser, setCurrentUser] = useState(null);
  const [selectedMovie, setSelectedMovie] = useState(null);
  const [selectedShowtime, setSelectedShowtime] = useState(null);

  // Modals
  const [filterModalOpen, setFilterModalOpen] = useState(false);

  // Toast notification
  const [toast, setToast] = useState(null);

  // Favorites stored in localStorage
  const [favorites, setFavorites] = useState(() => {
    try {
      const saved = localStorage.getItem('ces_favorites');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const showNotification = useCallback((message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => {
      setToast(null);
    }, 3200);
  }, []);

  useEffect(() => {
    let ignore = false;
    getCurrentUser()
      .then(async (user) => {
        if (!ignore && user.userId) {
          if (user.role === 'ADMIN') {
            await verifyAdminSession();
          }
          if (ignore) return;
          setCurrentUser(user);
          if (user.role === 'ADMIN') {
            setActivePage('admin-portal');
          }
        }
      })
      .catch((error) => {
        console.error('Unable to restore the signed-in session', error);
      });
    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    if (!currentUser) return undefined;
    let ignore = false;
    getFavoriteMovies()
      .then((savedMovies) => {
        if (!ignore) {
          setFavorites(savedMovies.map((movie) => movie.movieId));
        }
      })
      .catch((error) => {
        showNotification(error.message || 'Could not load your favorites.', 'error');
      });
    return () => {
      ignore = true;
    };
  }, [currentUser, showNotification]);

  // Save favorites to localStorage
  useEffect(() => {
    if (currentUser) return;
    try {
      localStorage.setItem('ces_favorites', JSON.stringify(favorites));
    } catch (e) {
      console.error('Failed to save favorites to localStorage', e);
    }
  }, [currentUser, favorites]);

  // Load initial movies from backend / fallback
  useEffect(() => {
    let ignore = false;
    async function init() {
      const result = await getAllMovies();
      if (!ignore) {
        setAllMovies(result.data);
        setMovies(result.data);
        setIsLiveBackend(result.isLive);
        setLoading(false);
      }
    }
    init();
    return () => {
      ignore = true;
    };
  }, []);

  // Manual reload function for refresh/reset
  const reloadAll = useCallback(async () => {
    setLoading(true);
    const result = await getAllMovies();
    setAllMovies(result.data);
    setMovies(result.data);
    setIsLiveBackend(result.isLive);
    setLoading(false);
  }, []);

  // Execute search or filter
  const applyFilters = useCallback(
    async (query, genre) => {
      setLoading(true);
      if (query && query.trim()) {
        const res = await searchMoviesByTitle(query.trim());
        let filtered = res.data;
        if (genre && genre !== 'ALL') {
          filtered = filtered.filter(
            (m) => m.genre.toLowerCase() === genre.toLowerCase()
          );
        }
        setMovies(filtered);
        setIsLiveBackend(res.isLive);
      } else if (genre && genre !== 'ALL') {
        const res = await filterMoviesByGenre(genre);
        setMovies(res.data);
        setIsLiveBackend(res.isLive);
      } else {
        const res = await getAllMovies();
        setMovies(res.data);
        setIsLiveBackend(res.isLive);
      }
      setLoading(false);
    },
    []
  );

  const handleSearch = (query) => {
    setSearchQuery(query);
    setShowFavoritesOnly(false);
    applyFilters(query, selectedGenre);
  };

  const handleSelectGenre = (genre) => {
    setSelectedGenre(genre);
    setShowFavoritesOnly(false);
    applyFilters(searchQuery, genre);
  };

  const handleSelectDate = (date) => {
    setSelectedDate(date);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setSelectedGenre('ALL');
    setSelectedDate('ALL');
    setShowFavoritesOnly(false);
    reloadAll();
    showNotification('Filters reset', 'info');
  };

  const toggleFavorite = async (movieId, movieTitle) => {
    const isFav = favorites.includes(movieId);
    if (currentUser) {
      try {
        if (isFav) {
          await removeFavoriteMovie(movieId);
        } else {
          await addFavoriteMovie(movieId);
        }
        setFavorites((previous) => (
          isFav ? previous.filter((id) => id !== movieId) : [...previous, movieId]
        ));
        showNotification(
          isFav ? `Removed "${movieTitle}" from favorites` : `Added "${movieTitle}" to favorites`,
          isFav ? 'info' : 'success'
        );
      } catch (error) {
        showNotification(error.message || 'Could not update your favorites.', 'error');
      }
      return;
    }

    setFavorites((prev) => {
      if (prev.includes(movieId)) {
        showNotification(`Removed "${movieTitle}" from favorites`, 'info');
        return prev.filter((id) => id !== movieId);
      } else {
        showNotification(`Added "${movieTitle}" to favorites`, 'success');
        return [...prev, movieId];
      }
    });
  };

  const isFavorite = (movieId) => favorites.includes(movieId);

  // Navigation handlers
  const openMovieDetail = (movie) => {
    setSelectedMovie(movie);
    setActivePage('movie-detail');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const openBooking = (movie, showtime) => {
    setSelectedMovie(movie);
    setSelectedShowtime(showtime);
    setActivePage('booking');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const navigateHome = () => {
    setActivePage('home');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const navigateRegister = () => {
    setActivePage('register');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const navigateLogin = useCallback(() => {
    setActivePage('login');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  const navigateForgotPassword = () => {
    setActivePage('forgot-password');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const completeLogin = async (user) => {
    if (user.role === 'ADMIN') {
      await verifyAdminSession();
    }
    setCurrentUser(user);
    setActivePage(user.role === 'ADMIN' ? 'admin-portal' : 'home');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const logout = async () => {
    try {
      await logoutAccount();
      setCurrentUser(null);
      try {
        const saved = localStorage.getItem('ces_favorites');
        setFavorites(saved ? JSON.parse(saved) : []);
      } catch (error) {
        console.error('Failed to restore guest favorites after logout', error);
        setFavorites([]);
      }
      setActivePage('home');
      showNotification('You have been signed out.', 'success');
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (error) {
      showNotification(error.message || 'Could not sign out. Please try again.', 'error');
    }
  };

  const navigateProfile = () => {
    if (!currentUser) {
      showNotification('Sign in to view and edit your profile.', 'info');
      navigateLogin();
      return;
    }
    setActivePage('profile');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <MovieContext.Provider
      value={{
        movies,
        allMovies,
        loading,
        isLiveBackend,
        searchQuery,
        selectedGenre,
        selectedDate,
        showFavoritesOnly,
        setShowFavoritesOnly,
        handleSearch,
        handleSelectGenre,
        handleSelectDate,
        clearFilters,
        favorites,
        toggleFavorite,
        isFavorite,
        activePage,
        selectedMovie,
        selectedShowtime,
        openMovieDetail,
        openBooking,
        navigateHome,
        navigateRegister,
        navigateLogin,
        navigateForgotPassword,
        navigateProfile,
        completeLogin,
        logout,
        currentUser,
        resetToken,
        setActivePage,
        filterModalOpen,
        setFilterModalOpen,
        toast,
        showNotification,
        refreshMovies: reloadAll
      }}
    >
      {children}
    </MovieContext.Provider>
  );
}

export function useMovieContext() {
  const context = useContext(MovieContext);
  if (!context) {
    throw new Error('useMovieContext must be used within a MovieProvider');
  }
  return context;
}
