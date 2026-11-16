package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.model.Movie;
import com.cinema.cinema_backend.controller.CreateMovieRequest;
import com.cinema.cinema_backend.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> searchMovies(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Movie> filterByGenre(String genre) {
        return movieRepository.findByGenreIgnoreCase(genre);
    }

    public List<Movie> getMoviesByStatus(String status) {
        return movieRepository.findByStatus(status);
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id).orElse(null);
    }

    public Movie createMovie(CreateMovieRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.title().trim());
        movie.setGenre(request.genre().trim());
        movie.setRating(blankToNull(request.rating()));
        movie.setDescription(request.description().trim());
        movie.setPosterUrl(blankToNull(request.posterUrl()));
        movie.setTrailerUrl(blankToNull(request.trailerUrl()));
        movie.setStatus(request.status());
        movie.setDirector(blankToNull(request.director()));
        movie.setCast(blankToNull(request.cast()));
        movie.setProducer(blankToNull(request.producer()));
        return movieRepository.save(movie);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}