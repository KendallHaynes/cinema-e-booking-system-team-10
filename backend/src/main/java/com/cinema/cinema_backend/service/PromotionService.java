package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.CreatePromotionRequest;
import com.cinema.cinema_backend.controller.PromotionResponse;
import com.cinema.cinema_backend.model.Promotion;
import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.repository.PromotionRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class PromotionService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, uuuu", Locale.US);

    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public PromotionService(
            PromotionRepository promotionRepository,
            UserRepository userRepository,
            JavaMailSender mailSender,
            @Value("${app.mail-from}") String fromAddress
    ) {
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Transactional
    public PromotionResponse createAndNotifySubscribers(CreatePromotionRequest request) {
        LocalDate today = LocalDate.now();
        if (request.startDate().isBefore(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promotion start date cannot be in the past.");
        }
        if (request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promotion end date must be on or after its start date.");
        }

        Promotion promotion = promotionRepository.saveAndFlush(new Promotion(
                request.name().trim(),
                request.discountPercentage(),
                request.startDate(),
                request.endDate()
        ));

        List<User> recipients = userRepository.findByPromotionsTrueAndCustomerStatus("ACTIVE");
        int emailsSent = 0;
        int emailFailures = 0;
        for (User recipient : recipients) {
            try {
                mailSender.send(buildMessage(recipient, promotion));
                emailsSent++;
            } catch (MailException exception) {
                emailFailures++;
            }
        }

        return new PromotionResponse(
                promotion.getPromotionId(),
                promotion.getName(),
                promotion.getDiscountPercentage(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                emailsSent,
                emailFailures
        );
    }

    private SimpleMailMessage buildMessage(User recipient, Promotion promotion) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient.getEmail());
        message.setSubject("Cinema E-Booking promotion: " + promotion.getName());
        message.setText("""
                Hello %s,

                A promotion you may be interested in is available:

                %s
                Discount: %s%%
                Valid from: %s through %s

                You are receiving this message because you subscribed to promotion emails.
                """.formatted(
                recipient.getFirstName(),
                promotion.getName(),
                promotion.getDiscountPercentage().stripTrailingZeros().toPlainString(),
                DATE_FORMAT.format(promotion.getStartDate()),
                DATE_FORMAT.format(promotion.getEndDate())
        ));
        return message;
    }
}
