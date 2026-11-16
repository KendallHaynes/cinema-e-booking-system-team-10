package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.ShowSeatResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SessionSeatHolds {
    private static final String ATTRIBUTE = SessionSeatHolds.class.getName();
    // Show-seat IDs are unique across all screenings.
    private final Map<Long, Owner> owners = new HashMap<>();

    private class Owner implements HttpSessionBindingListener {
        Long showId;

        @Override
        public void valueUnbound(HttpSessionBindingEvent event) {
            synchronized (SessionSeatHolds.this) {
                owners.values().removeIf(owner -> owner == this);
            }
        }
    }

    public synchronized List<ShowSeatResponse> seatMap(HttpSession session, List<ShowSeatResponse> seats) {
        Owner current = (Owner) session.getAttribute(ATTRIBUTE);
        return seats.stream().map(seat -> {
            Owner holder = owners.get(seat.showSeatId());
            return new ShowSeatResponse(seat.showSeatId(), seat.seatLabel(),
                    seat.available() && (holder == null || holder == current));
        }).toList();
    }

    public synchronized void select(HttpSession session, Long showId, List<Long> requested,
                                   List<ShowSeatResponse> seats) {
        Set<Long> ids = new HashSet<>(requested);
        if (ids.size() != requested.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select each seat only once.");
        }
        long available = seatMap(session, seats).stream()
                .filter(seat -> seat.available() && ids.contains(seat.showSeatId())).count();
        if (available != ids.size()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A selected seat is unavailable. Choose another seat.");
        }
        session.removeAttribute("checkoutShowId");
        session.removeAttribute("checkoutEmail");
        session.removeAttribute(ATTRIBUTE);
        if (!ids.isEmpty()) {
            Owner owner = new Owner();
            owner.showId = showId;
            session.setAttribute(ATTRIBUTE, owner);
            ids.forEach(id -> owners.put(id, owner));
        }
    }

    public synchronized void requireSelection(HttpSession session, Long showId) {
        Owner current = (Owner) session.getAttribute(ATTRIBUTE);
        if (current == null || !showId.equals(current.showId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Select seats for this showtime first.");
        }
    }
}
