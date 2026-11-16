package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.CreatePromotionRequest;
import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.repository.PromotionRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    private PromotionService promotionService;

    @BeforeEach
    void setUp() {
        promotionService = new PromotionService(
                promotionRepository,
                userRepository,
                mailSender,
                "promotions@example.com"
        );
    }

    @Test
    void createsPromotionAndEmailsOnlyActiveSubscribedRecipients() {
        User subscribedCustomer = new User(
                "Alex", "Customer", "alex@example.com", "hash", true, "CUSTOMER", "ACTIVE"
        );
        when(promotionRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            var promotion = invocation.<com.cinema.cinema_backend.model.Promotion>getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(promotion, "promotionId", 42L);
            return promotion;
        });
        when(userRepository.findByPromotionsTrueAndCustomerStatus("ACTIVE"))
                .thenReturn(List.of(subscribedCustomer));

        var result = promotionService.createAndNotifySubscribers(request(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(7)
        ));

        assertEquals(42L, result.promotionId());
        assertEquals(1, result.emailsSent());
        assertEquals(0, result.emailFailures());
        verify(userRepository).findByPromotionsTrueAndCustomerStatus("ACTIVE");
        ArgumentCaptor<SimpleMailMessage> emailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(emailCaptor.capture());
        assertEquals("alex@example.com", emailCaptor.getValue().getTo()[0]);
        assertEquals("promotions@example.com", emailCaptor.getValue().getFrom());
    }

    @Test
    void rejectsPastStartDateWithoutSavingOrSending() {
        assertThrows(
                ResponseStatusException.class,
                () -> promotionService.createAndNotifySubscribers(request(
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(1)
                ))
        );

        verify(promotionRepository, never()).saveAndFlush(any());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void rejectsEndDateBeforeStartDateWithoutSavingOrSending() {
        assertThrows(
                ResponseStatusException.class,
                () -> promotionService.createAndNotifySubscribers(request(
                        LocalDate.now().plusDays(2),
                        LocalDate.now().plusDays(1)
                ))
        );

        verify(promotionRepository, never()).saveAndFlush(any());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void reportsSubscriberEmailFailuresAfterSavingPromotion() {
        User subscribedCustomer = new User(
                "Alex", "Customer", "alex@example.com", "hash", true, "CUSTOMER", "ACTIVE"
        );
        when(promotionRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            var promotion = invocation.<com.cinema.cinema_backend.model.Promotion>getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(promotion, "promotionId", 42L);
            return promotion;
        });
        when(userRepository.findByPromotionsTrueAndCustomerStatus("ACTIVE"))
                .thenReturn(List.of(subscribedCustomer));
        doThrow(new MailSendException("SMTP unavailable"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        var result = promotionService.createAndNotifySubscribers(request(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(7)
        ));

        assertEquals(0, result.emailsSent());
        assertEquals(1, result.emailFailures());
    }

    private CreatePromotionRequest request(LocalDate startDate, LocalDate endDate) {
        return new CreatePromotionRequest(
                "Spring Movie Special",
                new BigDecimal("15.00"),
                startDate,
                endDate
        );
    }
}
