package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.PaymentCardRequest;
import com.cinema.cinema_backend.model.Favorite;
import com.cinema.cinema_backend.model.Movie;
import com.cinema.cinema_backend.model.ProfileAddress;
import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.repository.FavoriteRepository;
import com.cinema.cinema_backend.repository.MovieRepository;
import com.cinema.cinema_backend.repository.PaymentCardRepository;
import com.cinema.cinema_backend.repository.ProfileAddressRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_STREET = "100 Demo Street";
    private static final List<String> TEST_CARD_NUMBERS = List.of(
            "4242424242424242",
            "5555555555554444",
            "378282246310005"
    );

    private final boolean enabled;
    private final String demoPassword;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final FavoriteRepository favoriteRepository;
    private final ProfileAddressRepository addressRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;

    public DemoDataSeeder(
            @Value("${DEMO_SEED_ENABLED:${app.demo-seed.enabled:false}}") boolean enabled,
            @Value("${DEMO_SEED_PASSWORD:${app.demo-seed.password:}}") String demoPassword,
            UserRepository userRepository,
            MovieRepository movieRepository,
            FavoriteRepository favoriteRepository,
            ProfileAddressRepository addressRepository,
            PaymentCardRepository paymentCardRepository,
            PasswordEncoder passwordEncoder,
            ProfileService profileService
    ) {
        this.enabled = enabled;
        this.demoPassword = demoPassword;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.favoriteRepository = favoriteRepository;
        this.addressRepository = addressRepository;
        this.paymentCardRepository = paymentCardRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileService = profileService;
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
        seedUser("Kendall", "Admin", "kndllhaynes@gmail.com",
                passwordHash, "ADMIN", null, null);
        User favoriteUser = seedUser("Kendall", "Favorite", "kch55499@uga.edu",
                passwordHash, "CUSTOMER", "ACTIVE", false);
        User cardUser = seedUser("Kendall", "Card", "kendallhaynes7730@gmail.com",
                passwordHash, "CUSTOMER", "ACTIVE", false);

        addFavoriteIfAvailable(favoriteUser);
        ensureDemoAddress(cardUser);
        seedCards(cardUser);

        System.out.printf(
                "Demo data is ready: 1 admin, 2 verified customers, and %d saved cards for the card demo account.%n",
                paymentCardRepository.countByUser_UserId(cardUser.getUserId())
        );
    }

    private User seedUser(
            String firstName,
            String lastName,
            String email,
            String passwordHash,
            String userType,
            String customerStatus,
            Boolean promotions
    ) {
        User user = userRepository.findByEmail(email).orElseGet(
                () -> new User(firstName, lastName, email, passwordHash, promotions, userType, customerStatus)
        );
        user.configureDemoAccount(firstName, lastName, passwordHash, userType, customerStatus, promotions);
        return userRepository.save(user);
    }

    private void addFavoriteIfAvailable(User user) {
        Movie movie = movieRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Demo seeding needs at least one movie in the database to create the favorite."
                ));
        if (!favoriteRepository.existsByUser_UserIdAndMovie_MovieId(user.getUserId(), movie.getMovieId())) {
            favoriteRepository.save(new Favorite(user, movie));
        }
    }

    private void ensureDemoAddress(User user) {
        if (addressRepository.findByUser_UserId(user.getUserId()).isEmpty()) {
            addressRepository.save(new ProfileAddress(
                    user, DEMO_STREET, "Demo City", "GA", "30601", "USA"
            ));
        }
    }

    private void seedCards(User user) {
        long existingCards = paymentCardRepository.countByUser_UserId(user.getUserId());
        YearMonth expiration = YearMonth.now().plusYears(2);
        for (String cardNumber : TEST_CARD_NUMBERS) {
            if (existingCards >= 3) {
                return;
            }
            profileService.addPaymentCard(user.getUserId(), new PaymentCardRequest(
                    cardNumber,
                    expiration.getMonthValue(),
                    expiration.getYear()
            ));
            existingCards++;
        }
    }
}
