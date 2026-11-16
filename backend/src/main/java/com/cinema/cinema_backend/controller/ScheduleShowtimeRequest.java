package com.cinema.cinema_backend.controller;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ScheduleShowtimeRequest(
        @NotNull Long movieId,
        @NotNull Long roomId,
        @NotNull LocalDateTime dateTime
) {
}
