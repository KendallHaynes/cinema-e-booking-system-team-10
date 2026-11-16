package com.cinema.cinema_backend.controller;

public record ShowSeatResponse(Long showSeatId, String seatLabel, boolean available) {
}
