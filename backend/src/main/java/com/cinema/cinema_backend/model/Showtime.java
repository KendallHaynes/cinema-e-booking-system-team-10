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
import java.time.LocalDateTime;

@Entity
@Table(name = "shows")
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "showID")
    private Long showId;

    @Column(name = "dateTime", nullable = false)
    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movieID", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roomID", nullable = false)
    private Showroom showroom;

    protected Showtime() {
    }

    public Showtime(LocalDateTime dateTime, Movie movie, Showroom showroom) {
        this.dateTime = dateTime;
        this.movie = movie;
        this.showroom = showroom;
    }

    public Long getShowId() {
        return showId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Movie getMovie() {
        return movie;
    }

    public Showroom getShowroom() {
        return showroom;
    }
}
