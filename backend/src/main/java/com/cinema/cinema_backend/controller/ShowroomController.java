package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.service.ShowtimeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/showrooms")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class ShowroomController {

    private final ShowtimeService showtimeService;

    public ShowroomController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping
    public List<ShowroomResponse> getShowrooms() {
        return showtimeService.getShowrooms();
    }
}
