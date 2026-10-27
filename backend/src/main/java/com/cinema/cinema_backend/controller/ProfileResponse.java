package com.cinema.cinema_backend.controller;

import java.util.List;

public record ProfileResponse(
        String firstName,
        String lastName,
        String email,
        AddressResponse address,
        List<PaymentCardResponse> paymentCards
) {
}
