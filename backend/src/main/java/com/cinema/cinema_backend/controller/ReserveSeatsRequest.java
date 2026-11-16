package com.cinema.cinema_backend.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReserveSeatsRequest(
        @NotEmpty @Size(max = 8) List<@NotNull @Positive Long> showSeatIds,
        @Min(1) @Max(8) int ticketCount
) {
}
