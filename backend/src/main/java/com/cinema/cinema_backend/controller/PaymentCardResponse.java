package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.model.PaymentCard;

import java.time.LocalDate;

public record PaymentCardResponse(
        Long cardId,
        String brand,
        String lastFour,
        LocalDate expirationDate
) {
    public static PaymentCardResponse from(PaymentCard card) {
        return new PaymentCardResponse(
                card.getCardId(),
                card.getBrand(),
                card.getLastFour(),
                card.getExpirationDate()
        );
    }
}
