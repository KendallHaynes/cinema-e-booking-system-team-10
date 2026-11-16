package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.model.Movie;
import com.cinema.cinema_backend.repository.UserRepository;
import com.cinema.cinema_backend.service.MovieService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class MovieController {

    private final MovieService movieService;
    private final UserRepository userRepository;

    public MovieController(MovieService movieService, UserRepository userRepository) {
        this.movieService = movieService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }

    @GetMapping("/{id}")
    public Movie getMovieById(@PathVariable Long id) {
        return movieService.getMovieById(id);
    }

    @GetMapping("/search")
    public List<Movie> searchMovies(@RequestParam String title) {
        return movieService.searchMovies(title);
    }

    @GetMapping("/genre")
    public List<Movie> filterByGenre(@RequestParam String genre) {
        return movieService.filterByGenre(genre);
    }

    @GetMapping("/status")
    public List<Movie> getMoviesByStatus(@RequestParam String status) {
        return movieService.getMoviesByStatus(status);
    }

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public Movie createMovie(
            @Valid @RequestBody CreateMovieRequest request,
            HttpSession session
    ) {
        AdminAccess.requireAdministrator(session, userRepository);
        return movieService.createMovie(request);
    }
}