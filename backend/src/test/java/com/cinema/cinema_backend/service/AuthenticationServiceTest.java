package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.ForgotPasswordRequest;
import com.cinema.cinema_backend.controller.LoginRequest;
import com.cinema.cinema_backend.controller.ResetPasswordRequest;
import com.cinema.cinema_backend.model.PasswordResetToken;
import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.repository.PasswordResetTokenRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository resetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository,
                resetTokenRepository,
                passwordEncoder,
                mailSender,
                "http://localhost:5173",
                "noreply@cinema.local"
        );
    }

    @Test
    void loginReturnsCustomerRoleForActiveCustomer() {
        User user = new User("Jane", "Doe", "jane@example.com", "bcrypt-hash", false, "CUSTOMER", "ACTIVE");
        ReflectionTestUtils.setField(user, "userId", 1L);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secure!Password1", "bcrypt-hash")).thenReturn(true);

        var result = authenticationService.login(new LoginRequest(" JANE@example.com ", "Secure!Password1"));

        assertEquals("CUSTOMER", result.get("role"));
        assertEquals("1", result.get("userId"));
        assertEquals("Jane Doe", result.get("name"));
    }

    @Test
    void loginReturnsAdminRoleForAdmin() {
        User user = new User("Alex", "Admin", "admin@example.com", "bcrypt-hash", null, "ADMIN", null);
        ReflectionTestUtils.setField(user, "userId", 2L);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secure!Password1", "bcrypt-hash")).thenReturn(true);

        var result = authenticationService.login(new LoginRequest("admin@example.com", "Secure!Password1"));

        assertEquals("ADMIN", result.get("role"));
    }

    @Test
    void loginRejectsUnknownEmailAndWrongPasswordWithSameUnauthorizedStatus() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResponseStatusException missing = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.login(new LoginRequest("missing@example.com", "wrong"))
        );
        assertEquals(HttpStatus.UNAUTHORIZED, missing.getStatusCode());

        User user = new User("Jane", "Doe", "jane@example.com", "bcrypt-hash", false, "CUSTOMER", "ACTIVE");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "bcrypt-hash")).thenReturn(false);
        ResponseStatusException incorrect = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.login(new LoginRequest("jane@example.com", "wrong"))
        );
        assertEquals(HttpStatus.UNAUTHORIZED, incorrect.getStatusCode());
        assertEquals(missing.getReason(), incorrect.getReason());
    }

    @Test
    void loginRequiresEmailVerificationForInactiveCustomer() {
        User user = new User("Jane", "Doe", "jane@example.com", "bcrypt-hash", false, "CUSTOMER", "INACTIVE");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secure!Password1", "bcrypt-hash")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.login(new LoginRequest("jane@example.com", "Secure!Password1"))
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Confirm your email"));
    }

    @Test
    void loginBlocksSuspendedCustomer() {
        User user = new User("Jane", "Doe", "jane@example.com", "bcrypt-hash", false, "CUSTOMER", "SUSPENDED");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secure!Password1", "bcrypt-hash")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.login(new LoginRequest("jane@example.com", "Secure!Password1"))
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("suspended"));
    }

    @Test
    void forgotPasswordSendsResetLinkForExistingAccount() {
        User user = new User("Jane", "Doe", "jane@example.com", "bcrypt-hash", false, "CUSTOMER", "ACTIVE");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));

        authenticationService.requestPasswordReset(new ForgotPasswordRequest(" JANE@example.com "));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        assertEquals("jane@example.com", messageCaptor.getValue().getTo()[0]);
        assertTrue(messageCaptor.getValue().getText().contains("http://localhost:5173/?reset="));
        verify(resetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void forgotPasswordDoesNotSendEmailForUnknownAccount() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        authenticationService.requestPasswordReset(new ForgotPasswordRequest("missing@example.com"));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(resetTokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPasswordUpdatesHashAndConsumesToken() {
        User user = new User("Jane", "Doe", "jane@example.com", "old-hash", false, "CUSTOMER", "ACTIVE");
        PasswordResetToken token = new PasswordResetToken("token-hash", Instant.now().plusSeconds(600), user);
        when(resetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("New!Password123")).thenReturn("new-hash");

        authenticationService.resetPassword(new ResetPasswordRequest(
                "raw-token",
                "New!Password123",
                "New!Password123"
        ));

        assertEquals("new-hash", user.getPasswordHash());
        verify(userRepository).save(user);
        verify(resetTokenRepository).delete(token);
    }

    @Test
    void resetPasswordRejectsExpiredToken() {
        PasswordResetToken token = new PasswordResetToken(
                "token-hash",
                Instant.now().minusSeconds(1),
                new User("Jane", "Doe", "jane@example.com", "old-hash", false)
        );
        when(resetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.resetPassword(new ResetPasswordRequest(
                        "raw-token",
                        "New!Password123",
                        "New!Password123"
                ))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPasswordRejectsUnknownToken() {
        when(resetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.resetPassword(new ResetPasswordRequest(
                        "unknown-token",
                        "New!Password123",
                        "New!Password123"
                ))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPasswordRejectsMismatchingConfirmation() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.resetPassword(new ResetPasswordRequest(
                        "raw-token",
                        "New!Password123",
                        "Different!Password123"
                ))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(resetTokenRepository, never()).findByTokenHash(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
