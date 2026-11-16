package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.ScheduleShowtimeRequest;
import com.cinema.cinema_backend.controller.ShowSeatResponse;
import com.cinema.cinema_backend.controller.ShowroomResponse;
import com.cinema.cinema_backend.controller.ShowtimeResponse;
import com.cinema.cinema_backend.model.Movie;
import com.cinema.cinema_backend.model.Seat;
import com.cinema.cinema_backend.model.ShowSeat;
import com.cinema.cinema_backend.model.Showroom;
import com.cinema.cinema_backend.model.Showtime;
import com.cinema.cinema_backend.repository.MovieRepository;
import com.cinema.cinema_backend.repository.SeatRepository;
import com.cinema.cinema_backend.repository.ShowSeatRepository;
import com.cinema.cinema_backend.repository.ShowroomRepository;
import com.cinema.cinema_backend.repository.ShowtimeRepository;
import com.cinema.cinema_backend.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final ShowroomRepository showroomRepository;
    private final MovieRepository movieRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final TicketRepository ticketRepository;

    public ShowtimeService(
            ShowtimeRepository showtimeRepository,
            ShowroomRepository showroomRepository,
            MovieRepository movieRepository,
            SeatRepository seatRepository,
            ShowSeatRepository showSeatRepository,
            TicketRepository ticketRepository
    ) {
        this.showtimeRepository = showtimeRepository;
        this.showroomRepository = showroomRepository;
        this.movieRepository = movieRepository;
        this.seatRepository = seatRepository;
        this.showSeatRepository = showSeatRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public List<ShowroomResponse> getShowrooms() {
        return showroomRepository.findAllByOrderByNameAsc().stream()
                .map(room -> new ShowroomResponse(room.getRoomId(), room.getName(), room.getCapacity()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimes(Long movieId) {
        List<Showtime> showtimes = movieId == null
                ? showtimeRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now())
                : showtimeRepository.findByMovieMovieIdAndDateTimeAfterOrderByDateTimeAsc(movieId, LocalDateTime.now());
        return showtimes.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ShowtimeResponse schedule(ScheduleShowtimeRequest request) {
        if (!request.dateTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Showtime must be in the future.");
        }
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Selected movie was not found."));
        Showroom showroom = showroomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Selected showroom was not found."));
        if (showtimeRepository.existsByShowroomRoomIdAndDateTime(showroom.getRoomId(), request.dateTime())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That showroom already has a show at this date and time.");
        }
        List<Seat> seats = seatRepository.findByShowroomRoomIdOrderBySeatRowAscSeatNumberAsc(showroom.getRoomId());
        if (seats.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This showroom has no configured seats.");
        }

        Showtime showtime = showtimeRepository.saveAndFlush(new Showtime(request.dateTime(), movie, showroom));
        showSeatRepository.saveAll(seats.stream().map(seat -> new ShowSeat(showtime, seat)).toList());
        return toResponse(showtime);
    }

    @Transactional(readOnly = true)
    public List<ShowSeatResponse> getSeats(Long showId) {
        if (!showtimeRepository.existsById(showId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime was not found.");
        }
        List<ShowSeat> showSeats = showSeatRepository.findByShowtimeShowIdOrderBySeatSeatRowAscSeatSeatNumberAsc(showId);
        Set<Long> bookedSeats = showSeats.isEmpty()
                ? Set.of()
                : ticketRepository.findBookedShowSeatIds(showSeats.stream().map(ShowSeat::getShowSeatId).toList());
        return showSeats.stream()
                .map(showSeat -> new ShowSeatResponse(
                        showSeat.getShowSeatId(),
                        showSeat.getSeat().getSeatRow() + showSeat.getSeat().getSeatNumber(),
                        showSeat.isAvailable() && !bookedSeats.contains(showSeat.getShowSeatId())
                ))
                .toList();
    }

    @Transactional
    public void reserveSeats(Long showId, List<Long> showSeatIds, int ticketCount) {
        if (!showtimeRepository.existsById(showId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime was not found.");
        }
        if (showSeatIds.size() != ticketCount) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The number of selected seats must match the number of tickets."
            );
        }
        if (showSeatIds.size() != new HashSet<>(showSeatIds).size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A seat can only be selected once.");
        }
        int reservedCount = showSeatRepository.reserveAvailableSeats(showId, showSeatIds);
        if (reservedCount != showSeatIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "One or more selected seats are already booked or belong to another showtime. Refresh the seat map and try again."
            );
        }
    }

    private ShowtimeResponse toResponse(Showtime showtime) {
        return new ShowtimeResponse(
                showtime.getShowId(),
                showtime.getMovie().getMovieId(),
                showtime.getMovie().getTitle(),
                showtime.getShowroom().getRoomId(),
                showtime.getShowroom().getName(),
                showtime.getDateTime()
        );
    }
}
