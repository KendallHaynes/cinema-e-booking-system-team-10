package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.ForgotPasswordRequest;
import com.cinema.cinema_backend.controller.LoginRequest;
import com.cinema.cinema_backend.controller.ResetPasswordRequest;
import com.cinema.cinema_backend.model.PasswordResetToken;
import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.repository.PasswordResetTokenRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthenticationService {

    private static final Duration RESET_TOKEN_LIFETIME = Duration.ofMinutes(30);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String INVALID_CREDENTIALS = "Email or password is incorrect.";
    private static final String PASSWORD_REQUIREMENTS =
            "Password must be 10 to 72 characters and include an uppercase letter, a lowercase letter, a number, and a symbol.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String fromAddress;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordResetTokenRepository resetTokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.mail-from}") String fromAddress
    ) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.fromAddress = fromAddress;
    }

    @Transactional(readOnly = true)
    public Map<String, String> login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }

        if (!"ADMIN".equals(user.getUserType())) {
            if ("INACTIVE".equals(user.getCustomerStatus())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Confirm your email address before signing in.");
            }
            if ("SUSPENDED".equals(user.getCustomerStatus())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is suspended. Contact support.");
            }
            if (!"ACTIVE".equals(user.getCustomerStatus())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, INVALID_CREDENTIALS);
            }
        }

        return toSessionUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<Map<String, String>> getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .filter(User::canSignIn)
                .map(AuthenticationService::toSessionUser);
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        userRepository.findByEmail(normalizeEmail(request.email())).ifPresent(user -> {
            resetTokenRepository.deleteByUser_UserId(user.getUserId());

            String rawToken = generateToken();
            resetTokenRepository.save(new PasswordResetToken(
                    hashToken(rawToken),
                    Instant.now().plus(RESET_TOKEN_LIFETIME),
                    user
            ));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(user.getEmail());
            message.setSubject("Reset your Cinema E-Booking password");
            message.setText("""
                    Hello %s,

                    Use this link to reset your Cinema E-Booking password:
                    %s/?reset=%s

                    This link expires in 30 minutes. If you did not request a password reset, you can ignore this email.
                    """.formatted(user.getFirstName(), frontendUrl.replaceAll("/+$", ""), rawToken));
            mailSender.send(message);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords do not match.");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORD_REQUIREMENTS);
        }

        PasswordResetToken resetToken = resetTokenRepository.findByTokenHash(hashToken(request.token()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "This password reset link is invalid or has expired."
                ));

        if (resetToken.isExpired(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This password reset link is invalid or has expired."
            );
        }

        User user = resetToken.getUser();
        user.updatePasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        resetTokenRepository.delete(resetToken);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static Map<String, String> toSessionUser(User user) {
        return Map.of(
                "userId", user.getUserId().toString(),
                "email", user.getEmail(),
                "name", (user.getFirstName() + " " + user.getLastName()).trim(),
                "role", user.getUserType()
        );
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }
}
