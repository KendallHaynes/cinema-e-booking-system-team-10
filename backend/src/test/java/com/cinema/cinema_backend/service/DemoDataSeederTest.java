package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.model.Movie;
import com.cinema.cinema_backend.model.Seat;
import com.cinema.cinema_backend.model.Showroom;
import com.cinema.cinema_backend.repository.MovieRepository;
import com.cinema.cinema_backend.repository.SeatRepository;
import com.cinema.cinema_backend.repository.ShowSeatRepository;
import com.cinema.cinema_backend.repository.ShowroomRepository;
import com.cinema.cinema_backend.repository.ShowtimeRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ShowroomRepository showroomRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Test
    void seedsOnlyTheRequestedAdminAndStoresAnEncodedPassword() {
        when(passwordEncoder.encode("local-demo-password")).thenReturn("encoded-password");
        when(userRepository.findByEmail("kndllhaynes@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movieRepository.findAll(any(Sort.class))).thenReturn(List.of());
        DemoDataSeeder seeder = createSeeder(true, "local-demo-password");

        seeder.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User user = userCaptor.getValue();
        assertEquals("kndllhaynes@gmail.com", user.getEmail());
        assertEquals("ADMIN", user.getUserType());
        assertNull(user.getCustomerStatus());
        assertEquals("encoded-password", user.getPasswordHash());
        verify(userRepository).findByEmail("kndllhaynes@gmail.com");
        verify(userRepository, never()).findByEmail("kch55499@uga.edu");
        verify(userRepository, never()).findByEmail("kendallhaynes7730@gmail.com");
    }

    @Test
    void doesNotSeedWhenDisabled() {
        DemoDataSeeder seeder = createSeeder(false, "");

        seeder.run();

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createsThreeDefaultShowtimesAndSeatMapsForAMovieWithoutFutureShows() {
        Movie movie = new Movie();
        movie.setMovieId(5L);
        when(passwordEncoder.encode("local-demo-password")).thenReturn("encoded-password");
        when(userRepository.findByEmail("kndllhaynes@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movieRepository.findAll(any(Sort.class))).thenReturn(List.of(movie));

        Showroom first = org.mockito.Mockito.mock(Showroom.class);
        Showroom second = org.mockito.Mockito.mock(Showroom.class);
        Showroom third = org.mockito.Mockito.mock(Showroom.class);
        when(first.getRoomId()).thenReturn(1L);
        when(second.getRoomId()).thenReturn(2L);
        when(third.getRoomId()).thenReturn(3L);
        List<Showroom> rooms = List.of(first, second, third);
        when(showroomRepository.findAllByOrderByNameAsc()).thenReturn(rooms);
        for (Showroom room : rooms) {
            when(seatRepository.findByShowroomRoomIdOrderBySeatRowAscSeatNumberAsc(room.getRoomId()))
                    .thenReturn(List.of(org.mockito.Mockito.mock(Seat.class)));
        }
        when(showtimeRepository.findByMovieMovieIdAndDateTimeAfterOrderByDateTimeAsc(
                org.mockito.ArgumentMatchers.eq(5L),
                any()
        )).thenReturn(List.of());
        when(showtimeRepository.existsByShowroomRoomIdAndDateTime(any(), any())).thenReturn(false);
        when(showtimeRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        createSeeder(true, "local-demo-password").run();

        verify(showtimeRepository, org.mockito.Mockito.times(3)).saveAndFlush(any());
        verify(showSeatRepository, org.mockito.Mockito.times(3)).saveAll(any());
    }

    private DemoDataSeeder createSeeder(boolean enabled, String password) {
        return new DemoDataSeeder(
                enabled,
                password,
                userRepository,
                passwordEncoder,
                movieRepository,
                showroomRepository,
                seatRepository,
                showtimeRepository,
                showSeatRepository
        );
    }
}
