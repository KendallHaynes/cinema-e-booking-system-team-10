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
@Table(name = "show_seats", uniqueConstraints = {
        @UniqueConstraint(name = "unique_show_seat", columnNames = {"showID", "seatID"})
})
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "showSeatID")
    private Long showSeatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "showID", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seatID", nullable = false)
    private Seat seat;

    @Column(nullable = false)
    private boolean available = true;

    protected ShowSeat() {
    }

    public ShowSeat(Showtime showtime, Seat seat) {
        this.showtime = showtime;
        this.seat = seat;
    }

    public Long getShowSeatId() {
        return showSeatId;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public Seat getSeat() {
        return seat;
    }

    public boolean isAvailable() {
        return available;
    }
}
