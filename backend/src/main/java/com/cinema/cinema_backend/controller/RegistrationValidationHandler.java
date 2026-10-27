package com.cinema.cinema_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = {
        RegistrationController.class,
        AuthenticationController.class,
        ProfileController.class,
        FavoriteController.class,
        AdminSessionController.class
})
public class RegistrationValidationHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRegistration(MethodArgumentNotValidException exception) {
        var fieldErrors = exception.getBindingResult().getFieldErrors();
        boolean invalidPassword = fieldErrors.stream()
                .anyMatch(error -> error.getField().equals("password"));
        String objectName = exception.getBindingResult().getObjectName();
        String message;

        if (invalidPassword && ("registrationRequest".equals(objectName) || "resetPasswordRequest".equals(objectName))) {
            message = "Password must be 10 to 72 characters and include an uppercase letter, a lowercase letter, a number, and a symbol.";
        } else if ("loginRequest".equals(objectName)) {
            message = "Please enter a valid email address and password.";
        } else if ("forgotPasswordRequest".equals(objectName)) {
            message = "Please enter a valid email address.";
        } else if ("changePasswordRequest".equals(objectName)) {
            message = "New password must be 10 to 72 characters and include an uppercase letter, a lowercase letter, a number, and a symbol.";
        } else if ("paymentCardRequest".equals(objectName)) {
            message = "Enter a valid card number and expiration date.";
        } else if ("profileUpdateRequest".equals(objectName)) {
            message = "Please complete your required profile fields.";
        } else {
            message = "Please provide a valid first name, last name, email address, and password confirmation.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleRegistrationError(ResponseStatusException exception) {
        String message = exception.getReason() != null
                ? exception.getReason()
                : "The registration request could not be completed.";
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("message", message));
    }
}
