package com.cinema.cinema_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotionID")
    private Long promotionId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "discountPercentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "endDate", nullable = false)
    private LocalDate endDate;

    protected Promotion() {
    }

    public Promotion(String name, BigDecimal discountPercentage, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.discountPercentage = discountPercentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getPromotionId() {
        return promotionId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
