/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import {
  getAllMovies,
  searchMoviesByTitle,
  filterMoviesByGenre
} from '../api/movieApi';

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
  const [activePage, setActivePage] = useState('home'); // 'home' | 'movie-detail' | 'booking'
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

  // Save favorites to localStorage
  useEffect(() => {
    try {
      localStorage.setItem('ces_favorites', JSON.stringify(favorites));
    } catch (e) {
      console.error('Failed to save favorites to localStorage', e);
    }
  }, [favorites]);

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

  const toggleFavorite = (movieId, movieTitle) => {
    setFavorites((prev) => {
      const isFav = prev.includes(movieId);
      if (isFav) {
        showNotification(`Removed "${movieTitle}" from favorites`, 'info');
        return prev.filter((id) => id !== movieId);
      } else {
        showNotification(`Added "${movieTitle}" to favorites! ⭐`, 'success');
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
