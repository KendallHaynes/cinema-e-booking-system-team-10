package com.cinema.cinema_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Long userId;

    @Column(name = "firstName", nullable = false, length = 100)
    private String firstName;

    @Column(name = "lastName", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "userType", nullable = false, length = 20)
    private String userType = "CUSTOMER";

    @Column(name = "customerStatus", length = 20)
    private String customerStatus = "INACTIVE";

    private Boolean promotions;

    protected User() {
    }

    public User(String firstName, String lastName, String email, String passwordHash, Boolean promotions) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.promotions = promotions;
    }

    public User(
            String firstName,
            String lastName,
            String email,
            String passwordHash,
            Boolean promotions,
            String userType,
            String customerStatus
    ) {
        this(firstName, lastName, email, passwordHash, promotions);
        this.userType = userType;
        this.customerStatus = customerStatus;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getUserType() {
        return userType;
    }

    public String getCustomerStatus() {
        return customerStatus;
    }

    public Boolean getPromotions() {
        return promotions;
    }

    public void activate() {
        customerStatus = "ACTIVE";
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void configureDemoAccount(
            String firstName,
            String lastName,
            String passwordHash,
            String userType,
            String customerStatus,
            Boolean promotions
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
        this.userType = userType;
        this.customerStatus = customerStatus;
        this.promotions = promotions;
    }

    public void updateProfile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public boolean canSignIn() {
        return "ADMIN".equals(userType) || "ACTIVE".equals(customerStatus);
    }
}
