package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.model.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    List<ShowSeat> findByShowtimeShowIdOrderBySeatSeatRowAscSeatSeatNumberAsc(Long showId);
}
