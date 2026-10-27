package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.model.Movie;
import com.cinema.cinema_backend.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<Movie> getFavorites(HttpSession session) {
        return favoriteService.getFavorites(SessionAccount.requireUserId(session));
    }

    @PostMapping("/{movieId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Long movieId, HttpSession session) {
        favoriteService.addFavorite(SessionAccount.requireUserId(session), movieId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long movieId, HttpSession session) {
        favoriteService.removeFavorite(SessionAccount.requireUserId(session), movieId);
        return ResponseEntity.noContent().build();
    }
}
