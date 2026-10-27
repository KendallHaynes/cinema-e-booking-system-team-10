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
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

@Entity
@Table(name = "payment_cards")
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cardID")
    private Long cardId;

    @Column(name = "cardNumber", nullable = false, length = 512)
    private String encryptedCardNumber;

    @Column(name = "expirationDate", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "billingAddress", nullable = false, length = 512)
    private String billingAddress;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Column(length = 32)
    private String brand;

    @Column(name = "lastFour", length = 4)
    private String lastFour;

    protected PaymentCard() {
    }

    public PaymentCard(
            User user,
            String encryptedCardNumber,
            LocalDate expirationDate,
            String billingAddress,
            String brand,
            String lastFour
    ) {
        this.user = user;
        this.encryptedCardNumber = encryptedCardNumber;
        this.expirationDate = expirationDate;
        this.billingAddress = billingAddress;
        this.brand = brand;
        this.lastFour = lastFour;
    }

    public Long getCardId() {
        return cardId;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public String getBrand() {
        return brand;
    }

    public String getLastFour() {
        return lastFour;
    }

    @JsonIgnore
    public String getStoredCardNumber() {
        return encryptedCardNumber;
    }

    public void replaceStoredCardNumber(String encryptedCardNumber) {
        this.encryptedCardNumber = encryptedCardNumber;
    }

    public void setPresentationMetadata(String brand, String lastFour) {
        this.brand = brand;
        this.lastFour = lastFour;
    }

    public void updateBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }
}
