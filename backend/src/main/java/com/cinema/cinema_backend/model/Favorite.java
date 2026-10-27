package com.cinema.cinema_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
@IdClass(FavoriteId.class)
public class Favorite {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movieID", nullable = false)
    private Movie movie;

    private LocalDateTime dateAdded = LocalDateTime.now();

    protected Favorite() {
    }

    public Favorite(User user, Movie movie) {
        this.user = user;
        this.movie = movie;
        this.dateAdded = LocalDateTime.now();
    }

    public Movie getMovie() {
        return movie;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }
}
