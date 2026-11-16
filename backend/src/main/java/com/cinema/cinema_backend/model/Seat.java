package com.cinema.cinema_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(name = "unique_room_seat", columnNames = {"roomID", "row", "seatNumber"})
})
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seatID")
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roomID", nullable = false)
    private Showroom showroom;

    @Column(name = "`row`", nullable = false, length = 10)
    private String seatRow;

    @Column(name = "seatNumber", nullable = false)
    private int seatNumber;

    protected Seat() {
    }

    public Long getSeatId() {
        return seatId;
    }

    public Showroom getShowroom() {
        return showroom;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public int getSeatNumber() {
        return seatNumber;
    }
}
