package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.ChangePasswordRequest;
import com.cinema.cinema_backend.controller.PaymentCardRequest;
import com.cinema.cinema_backend.controller.ProfileUpdateRequest;
import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.repository.PaymentCardRepository;
import com.cinema.cinema_backend.repository.ProfileAddressRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileAddressRepository addressRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(
                userRepository,
                addressRepository,
                paymentCardRepository,
                passwordEncoder,
                new CardEncryptionService(""),
                mailSender,
                "noreply@example.com"
        );
    }

    @Test
    void changingPasswordRequiresAndVerifiesCurrentPassword() {
        User user = new User("Taylor", "Customer", "taylor@example.com", "old-hash", false, "CUSTOMER", "ACTIVE");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Old!Password123", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("New!Password123")).thenReturn("new-hash");

        profileService.changePassword(1L, new ChangePasswordRequest(
                "Old!Password123",
                "New!Password123",
                "New!Password123"
        ));

        assertEquals("new-hash", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void changingPasswordRejectsIncorrectCurrentPassword() {
        User user = new User("Taylor", "Customer", "taylor@example.com", "old-hash", false, "CUSTOMER", "ACTIVE");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> profileService.changePassword(1L, new ChangePasswordRequest(
                        "wrong",
                        "New!Password123",
                        "New!Password123"
                ))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refusesToSaveMoreThanThreePaymentCards() {
        User user = new User("Taylor", "Customer", "taylor@example.com", "hash", false, "CUSTOMER", "ACTIVE");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUser_UserId(1L)).thenReturn(3L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> profileService.addPaymentCard(
                        1L,
                        new PaymentCardRequest("4111111111111111", 12, 2030)
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void personalProfileChangesSendSecurityEmail() {
        User user = new User("Taylor", "Customer", "taylor@example.com", "hash", false, "CUSTOMER", "ACTIVE");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByUser_UserId(1L)).thenReturn(Optional.empty());
        when(paymentCardRepository.findAllByUser_UserIdOrderByCardId(1L)).thenReturn(List.of());

        profileService.updateProfile(1L, new ProfileUpdateRequest("Alex", "Customer", null));

        assertEquals("Alex", user.getFirstName());
        verify(userRepository).save(user);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
