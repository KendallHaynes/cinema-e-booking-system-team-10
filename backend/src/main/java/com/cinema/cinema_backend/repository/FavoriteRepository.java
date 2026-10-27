package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.model.Favorite;
import com.cinema.cinema_backend.model.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findAllByUser_UserIdOrderByDateAddedDesc(Long userId);
    boolean existsByUser_UserIdAndMovie_MovieId(Long userId, Long movieId);
    void deleteByUser_UserIdAndMovie_MovieId(Long userId, Long movieId);
}
