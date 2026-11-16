package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.ScheduleShowtimeRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private ShowroomRepository showroomRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private TicketRepository ticketRepository;

    private ShowtimeService showtimeService;

    @BeforeEach
    void setUp() {
        showtimeService = new ShowtimeService(
                showtimeRepository,
                showroomRepository,
                movieRepository,
                seatRepository,
                showSeatRepository,
                ticketRepository
        );
    }

    @Test
    void rejectsDuplicateRoomAndDateTime() {
        Showroom room = org.mockito.Mockito.mock(Showroom.class);
        when(room.getRoomId()).thenReturn(2L);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(new Movie()));
        when(showroomRepository.findById(2L)).thenReturn(Optional.of(room));
        when(showtimeRepository.existsByShowroomRoomIdAndDateTime(eq(2L), any())).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> showtimeService.schedule(new ScheduleShowtimeRequest(
                        1L,
                        2L,
                        LocalDateTime.now().plusDays(1)
                ))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(seatRepository, never()).findByShowroomRoomIdOrderBySeatRowAscSeatNumberAsc(2L);
    }

    @Test
    void marksPreviouslyBookedSeatsUnavailable() {
        var showtime = org.mockito.Mockito.mock(Showtime.class);
        var seat = org.mockito.Mockito.mock(Seat.class);
        ShowSeat showSeat = new ShowSeat(showtime, seat);
        ReflectionTestUtils.setField(showSeat, "showSeatId", 12L);
        when(showtimeRepository.existsById(3L)).thenReturn(true);
        when(showSeatRepository.findByShowtimeShowIdOrderBySeatSeatRowAscSeatSeatNumberAsc(3L))
                .thenReturn(List.of(showSeat));
        when(ticketRepository.findBookedShowSeatIds(anyCollection())).thenReturn(Set.of(12L));
        when(seat.getSeatRow()).thenReturn("A");
        when(seat.getSeatNumber()).thenReturn(1);

        var result = showtimeService.getSeats(3L);

        assertEquals("A1", result.get(0).seatLabel());
        assertFalse(result.get(0).available());
        verify(ticketRepository).findBookedShowSeatIds(anyCollection());
    }
}
