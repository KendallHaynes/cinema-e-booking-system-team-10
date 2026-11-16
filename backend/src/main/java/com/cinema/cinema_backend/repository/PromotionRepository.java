package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
}
