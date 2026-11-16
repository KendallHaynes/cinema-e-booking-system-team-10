package com.cinema.cinema_backend.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromotionResponse(
        Long promotionId,
        String name,
        BigDecimal discountPercentage,
        LocalDate startDate,
        LocalDate endDate,
        int emailsSent,
        int emailFailures
) {
}
