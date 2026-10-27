package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.model.ProfileAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileAddressRepository extends JpaRepository<ProfileAddress, Long> {
    Optional<ProfileAddress> findByUser_UserId(Long userId);
}
