package com.cinema.cinema_backend.controller;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePromotionRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal discountPercentage,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
