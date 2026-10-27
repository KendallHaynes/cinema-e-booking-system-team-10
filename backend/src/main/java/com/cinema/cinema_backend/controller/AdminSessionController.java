package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.service.AuthenticationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class AdminSessionController {

    private final AuthenticationService authenticationService;

    public AdminSessionController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/session")
    public Map<String, String> verifyAdminSession(HttpSession session) {
        Long userId = SessionAccount.requireUserId(session);
        Map<String, String> user = authenticationService.getCurrentUser(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
        if (!"ADMIN".equals(user.get("role"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access is required.");
        }
        return Map.of("message", "Administrator access confirmed.");
    }
}
