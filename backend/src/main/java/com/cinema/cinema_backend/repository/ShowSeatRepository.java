package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.model.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    List<ShowSeat> findByShowtimeShowIdOrderBySeatSeatRowAscSeatSeatNumberAsc(Long showId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ShowSeat seat
            SET seat.available = false
            WHERE seat.showtime.showId = :showId
              AND seat.showSeatId IN :showSeatIds
              AND seat.available = true
            """)
    int reserveAvailableSeats(
            @Param("showId") Long showId,
            @Param("showSeatIds") List<Long> showSeatIds
    );
}
