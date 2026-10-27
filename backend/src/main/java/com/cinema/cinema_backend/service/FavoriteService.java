package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.model.Favorite;
import com.cinema.cinema_backend.model.Movie;
import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.repository.FavoriteRepository;
import com.cinema.cinema_backend.repository.MovieRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            UserRepository userRepository,
            MovieRepository movieRepository
    ) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional(readOnly = true)
    public List<Movie> getFavorites(Long userId) {
        requireActiveUser(userId);
        return favoriteRepository.findAllByUser_UserIdOrderByDateAddedDesc(userId)
                .stream()
                .map(Favorite::getMovie)
                .toList();
    }

    @Transactional
    public void addFavorite(Long userId, Long movieId) {
        if (favoriteRepository.existsByUser_UserIdAndMovie_MovieId(userId, movieId)) {
            return;
        }
        User user = requireActiveUser(userId);
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found."));
        favoriteRepository.save(new Favorite(user, movie));
    }

    @Transactional
    public void removeFavorite(Long userId, Long movieId) {
        requireActiveUser(userId);
        favoriteRepository.deleteByUser_UserIdAndMovie_MovieId(userId, movieId);
    }

    private User requireActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
        if (!user.canSignIn()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account cannot access favorites.");
        }
        return user;
    }
}
