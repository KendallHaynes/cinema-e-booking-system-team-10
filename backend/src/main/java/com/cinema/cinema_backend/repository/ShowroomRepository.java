package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.model.Showroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowroomRepository extends JpaRepository<Showroom, Long> {
    List<Showroom> findAllByOrderByNameAsc();
}
