package com.cinema.cinema_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "showrooms")
public class Showroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roomID")
    private Long roomId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int capacity;

    protected Showroom() {
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }
}
