package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.model.BookingTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

public interface TicketRepository extends JpaRepository<BookingTicket, Long> {

    @Query(value = "SELECT showSeatID FROM tickets WHERE showSeatID IN (:showSeatIds)", nativeQuery = true)
    Set<Long> findBookedShowSeatIds(@Param("showSeatIds") Collection<Long> showSeatIds);
}
