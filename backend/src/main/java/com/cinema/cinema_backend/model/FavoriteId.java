package com.cinema.cinema_backend.model;

import java.io.Serializable;
import java.util.Objects;

public class FavoriteId implements Serializable {

    private Long user;
    private Long movie;

    public FavoriteId() {
    }

    public FavoriteId(Long user, Long movie) {
        this.user = user;
        this.movie = movie;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FavoriteId that)) {
            return false;
        }
        return Objects.equals(user, that.user) && Objects.equals(movie, that.movie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, movie);
    }
}
