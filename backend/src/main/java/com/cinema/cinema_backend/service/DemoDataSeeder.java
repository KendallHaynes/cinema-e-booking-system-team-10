package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.model.Movie;
import com.cinema.cinema_backend.model.Seat;
import com.cinema.cinema_backend.model.ShowSeat;
import com.cinema.cinema_backend.model.Showroom;
import com.cinema.cinema_backend.model.Showtime;
import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.repository.MovieRepository;
import com.cinema.cinema_backend.repository.SeatRepository;
import com.cinema.cinema_backend.repository.ShowSeatRepository;
import com.cinema.cinema_backend.repository.ShowroomRepository;
import com.cinema.cinema_backend.repository.ShowtimeRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private final boolean enabled;
    private final String demoPassword;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MovieRepository movieRepository;
    private final ShowroomRepository showroomRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ShowSeatRepository showSeatRepository;

    public DemoDataSeeder(
            @Value("${DEMO_SEED_ENABLED:${app.demo-seed.enabled:false}}") boolean enabled,
            @Value("${DEMO_SEED_PASSWORD:${app.demo-seed.password:}}") String demoPassword,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            MovieRepository movieRepository,
            ShowroomRepository showroomRepository,
            SeatRepository seatRepository,
            ShowtimeRepository showtimeRepository,
            ShowSeatRepository showSeatRepository
    ) {
        this.enabled = enabled;
        this.demoPassword = demoPassword;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.movieRepository = movieRepository;
        this.showroomRepository = showroomRepository;
        this.seatRepository = seatRepository;
        this.showtimeRepository = showtimeRepository;
        this.showSeatRepository = showSeatRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (demoPassword.isBlank()) {
            throw new IllegalStateException(
                    "Demo seeding is enabled but DEMO_SEED_PASSWORD has not been set."
            );
        }

        String passwordHash = passwordEncoder.encode(demoPassword);
        seedUser("Kendall", "Admin", "kndllhaynes@gmail.com", passwordHash);
        seedDefaultShowtimes();
        System.out.println("Demo admin account is ready.");
    }

    private void seedDefaultShowtimes() {
        List<Movie> movies = movieRepository.findAll(Sort.by("movieId"));
        if (movies.isEmpty()) {
            return;
        }
        List<Showroom> showrooms = showroomRepository.findAllByOrderByNameAsc();
        if (showrooms.isEmpty()) {
            throw new IllegalStateException("Demo showtimes cannot be seeded because no showrooms are configured.");
        }
        List<List<Seat>> seatsByRoom = showrooms.stream()
                .map(room -> seatRepository.findByShowroomRoomIdOrderBySeatRowAscSeatNumberAsc(room.getRoomId()))
                .toList();
        if (seatsByRoom.stream().anyMatch(List::isEmpty)) {
            throw new IllegalStateException("Demo showtimes cannot be seeded because a showroom has no configured seats.");
        }

        LocalDate firstShowDate = LocalDate.now().plusDays(1);
        for (int movieIndex = 0; movieIndex < movies.size(); movieIndex++) {
            Movie movie = movies.get(movieIndex);
            int currentShowCount = showtimeRepository
                    .findByMovieMovieIdAndDateTimeAfterOrderByDateTimeAsc(
                            movie.getMovieId(),
                            LocalDateTime.now()
                    )
                    .size();
            int showIndex = 0;
            while (currentShowCount < 3) {
                int slot = movieIndex * 3 + showIndex++;
                int showroomsPerDay = showrooms.size() * 3;
                int dayOffset = slot / showroomsPerDay + 1;
                int slotInDay = slot % showroomsPerDay;
                int roomIndex = slotInDay % showrooms.size();
                int startHour = 14 + (slotInDay / showrooms.size()) * 3;
                Showroom showroom = showrooms.get(roomIndex);
                var dateTime = firstShowDate.plusDays(dayOffset - 1L).atTime(startHour, 0);

                if (showtimeRepository.existsByShowroomRoomIdAndDateTime(showroom.getRoomId(), dateTime)) {
                    continue;
                }

                Showtime showtime = showtimeRepository.saveAndFlush(new Showtime(dateTime, movie, showroom));
                showSeatRepository.saveAll(
                        seatsByRoom.get(roomIndex).stream()
                                .map(seat -> new ShowSeat(showtime, seat))
                                .toList()
                );
                currentShowCount++;
            }
        }
    }

    private User seedUser(
            String firstName,
            String lastName,
            String email,
            String passwordHash
    ) {
        User user = userRepository.findByEmail(email).orElseGet(
                () -> new User(firstName, lastName, email, passwordHash, null, "ADMIN", null)
        );
        user.configureDemoAccount(firstName, lastName, passwordHash, "ADMIN", null, null);
        return userRepository.save(user);
    }
}
