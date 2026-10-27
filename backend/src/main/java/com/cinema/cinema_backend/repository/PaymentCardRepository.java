package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.model.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {
    long countByUser_UserId(Long userId);
    List<PaymentCard> findAllByUser_UserIdOrderByCardId(Long userId);
    Optional<PaymentCard> findByCardIdAndUser_UserId(Long cardId, Long userId);

    @Query(value = "select count(*) from bookings where cardID = :cardId", nativeQuery = true)
    long countBookingsByCardId(@Param("cardId") Long cardId);
}
