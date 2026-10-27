package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.RegistrationRequest;
import com.cinema.cinema_backend.model.EmailVerificationToken;
import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.repository.EmailVerificationTokenRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
                userRepository,
                tokenRepository,
                passwordEncoder,
                mailSender,
                "http://localhost:5173/",
                "noreply@cinema.local"
        );
    }

    @Test
    void registerStoresInactiveCustomerAndSendsConfirmationEmail() {
        RegistrationRequest request = new RegistrationRequest(
                "  Jane ",
                " Doe ",
                " JANE@example.com ",
                "Secure!Password1",
                "Secure!Password1",
                false
        );
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secure!Password1")).thenReturn("bcrypt-hash");

        registrationService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("Jane", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("jane@example.com", savedUser.getEmail());
        assertEquals("bcrypt-hash", savedUser.getPasswordHash());
        assertEquals("CUSTOMER", savedUser.getUserType());
        assertEquals("INACTIVE", savedUser.getCustomerStatus());
        assertEquals(false, savedUser.getPromotions());

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertEquals("jane@example.com", messageCaptor.getValue().getTo()[0]);
        org.junit.jupiter.api.Assertions.assertTrue(
                messageCaptor.getValue().getText().contains("http://localhost:5173/?verify=")
        );
        verify(tokenRepository).save(any(EmailVerificationToken.class));
    }

    @Test
    void verifyEmailActivatesAccountAndConsumesToken() {
        User user = new User("Jane", "Doe", "jane@example.com", "bcrypt-hash", false);
        EmailVerificationToken token = new EmailVerificationToken(
                "token-hash",
                Instant.now().plusSeconds(3600),
                user
        );
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        registrationService.verifyEmail("raw-token");

        assertEquals("ACTIVE", user.getCustomerStatus());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void registerRejectsDuplicateEmailBeforeSaving() {
        RegistrationRequest request = new RegistrationRequest(
                "Jane",
                "Doe",
                "jane@example.com",
                "Secure!Password1",
                "Secure!Password1",
                false
        );
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> registrationService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("An account with this email already exists.", exception.getReason());
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
