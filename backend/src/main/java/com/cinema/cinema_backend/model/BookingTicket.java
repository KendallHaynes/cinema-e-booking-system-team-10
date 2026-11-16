package com.cinema.cinema_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class BookingTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticketID")
    private Long ticketId;

    @Column(name = "showSeatID", nullable = false)
    private Long showSeatId;

    protected BookingTicket() {
    }
}
