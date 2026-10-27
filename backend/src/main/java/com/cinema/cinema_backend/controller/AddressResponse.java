package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.model.ProfileAddress;

public record AddressResponse(
        String street,
        String city,
        String region,
        String postalCode,
        String country
) {
    public static AddressResponse from(ProfileAddress address) {
        return new AddressResponse(
                address.getStreet(),
                address.getCity(),
                address.getRegion(),
                address.getPostalCode(),
                address.getCountry()
        );
    }
}
