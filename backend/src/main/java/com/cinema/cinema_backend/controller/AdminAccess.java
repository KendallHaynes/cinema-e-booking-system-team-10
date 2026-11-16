package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class AdminAccess {

    private AdminAccess() {
    }

    public static void requireAdministrator(HttpSession session, UserRepository userRepository) {
        Long userId = SessionAccount.requireUserId(session);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
        if (!"ADMIN".equals(user.getUserType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access is required.");
        }
    }
}
