package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.service.ProfileService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileResponse getProfile(HttpSession session) {
        return profileService.getProfile(SessionAccount.requireUserId(session));
    }

    @PutMapping
    public ProfileResponse updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            HttpSession session
    ) {
        return profileService.updateProfile(SessionAccount.requireUserId(session), request);
    }

    @PostMapping("/cards")
    public ResponseEntity<PaymentCardResponse> addCard(
            @Valid @RequestBody PaymentCardRequest request,
            HttpSession session
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.addPaymentCard(SessionAccount.requireUserId(session), request));
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> removeCard(@PathVariable Long cardId, HttpSession session) {
        profileService.removePaymentCard(SessionAccount.requireUserId(session), cardId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpSession session
    ) {
        profileService.changePassword(SessionAccount.requireUserId(session), request);
        return ResponseEntity.ok(Map.of("message", "Your password has been updated."));
    }
}
