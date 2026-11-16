package com.cinema.cinema_backend.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateMovieRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 100) String genre,
        @Size(max = 20) String rating,
        @NotBlank String description,
        @URL @Pattern(regexp = "(?i)^(?:https?://.+)?$") @Size(max = 500) String posterUrl,
        @URL @Pattern(regexp = "(?i)^(?:https?://.+)?$") @Size(max = 500) String trailerUrl,
        @NotBlank @Pattern(regexp = "CURRENTLY_RUNNING|COMING_SOON") String status,
        @Size(max = 255) String director,
        String cast,
        @Size(max = 255) String producer
) {
}
