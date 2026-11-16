package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.repository.UserRepository;
import com.cinema.cinema_backend.service.PromotionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class PromotionController {

    private final PromotionService promotionService;
    private final UserRepository userRepository;

    public PromotionController(PromotionService promotionService, UserRepository userRepository) {
        this.promotionService = promotionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionResponse createPromotion(
            @Valid @RequestBody CreatePromotionRequest request,
            HttpSession session
    ) {
        AdminAccess.requireAdministrator(session, userRepository);
        return promotionService.createAndNotifySubscribers(request);
    }
}
