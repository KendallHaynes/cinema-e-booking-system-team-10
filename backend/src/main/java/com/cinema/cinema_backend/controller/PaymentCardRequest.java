package com.cinema.cinema_backend.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentCardRequest(
        @NotBlank @Size(min = 13, max = 23) String cardNumber,
        @Min(1) @Max(12) int expirationMonth,
        @Min(2000) @Max(2100) int expirationYear
) {
}
