package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    List<Showtime> findByMovieMovieIdAndDateTimeAfterOrderByDateTimeAsc(Long movieId, LocalDateTime now);
    List<Showtime> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime now);
    boolean existsByShowroomRoomIdAndDateTime(Long roomId, LocalDateTime dateTime);
}
