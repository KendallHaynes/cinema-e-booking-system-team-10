package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.repository.UserRepository;
import com.cinema.cinema_backend.service.ShowtimeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.cinema.cinema_backend.service.SessionSeatHolds;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final UserRepository userRepository;

    private final SessionSeatHolds holds;

    public ShowtimeController(ShowtimeService showtimeService, UserRepository userRepository, SessionSeatHolds holds) {
        this.holds = holds;
        this.showtimeService = showtimeService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<ShowtimeResponse> getShowtimes(@RequestParam(required = false) Long movieId) {
        return showtimeService.getShowtimes(movieId);
    }

    @GetMapping("/{showId}/seats")
    public List<ShowSeatResponse> getSeats(@PathVariable Long showId, HttpSession session) {
        return holds.seatMap(session, showtimeService.getSeats(showId));
    }

    public record Selection(@NotNull @Size(max = 8) List<@NotNull @Positive Long> showSeatIds) {}

    @PutMapping("/{showId}/selection")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void select(@PathVariable Long showId, @Valid @RequestBody Selection request, HttpSession session) {
        holds.select(session, showId, request.showSeatIds(), showtimeService.getSeats(showId));
    }

    @PostMapping("/{showId}/reserve")
    public SeatReservationResponse reserveSeats(
            @PathVariable Long showId,
            @Valid @RequestBody ReserveSeatsRequest request,
            HttpSession session
    ) {
        Long userId = SessionAccount.requireUserId(session);
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
        if (!"CUSTOMER".equals(user.getUserType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customer accounts can reserve seats.");
        }
        if (request.showSeatIds().size() != request.ticketCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select one seat per ticket.");
        }
        holds.select(session, showId, request.showSeatIds(), showtimeService.getSeats(showId));
        session.setAttribute("checkoutShowId", showId);
        return new SeatReservationResponse(request.showSeatIds().size());
    }

    public record CheckoutEmail(@NotBlank @Email @Size(max = 255) String email) {}

    @PostMapping("/{showId}/checkout-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmEmail(@PathVariable Long showId, @Valid @RequestBody CheckoutEmail request, HttpSession session) {
        SessionAccount.requireUserId(session);
        if (!showId.equals(session.getAttribute("checkoutShowId"))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review your seat selection before continuing.");
        }
        holds.requireSelection(session, showId);
        session.setAttribute("checkoutEmail", request.email());
    }

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public ShowtimeResponse schedule(
            @Valid @RequestBody ScheduleShowtimeRequest request,
            HttpSession session
    ) {
        AdminAccess.requireAdministrator(session, userRepository);
        try {
            return showtimeService.schedule(request);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "That showroom already has a show at this date and time.",
                    exception
            );
        }
    }
}
