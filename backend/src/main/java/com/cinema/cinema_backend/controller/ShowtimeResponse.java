package com.cinema.cinema_backend.controller;

import java.time.LocalDateTime;

public record ShowtimeResponse(
        Long showId,
        Long movieId,
        String movieTitle,
        Long roomId,
        String roomName,
        LocalDateTime dateTime
) {
}
