package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.model.PaymentCard;
import com.cinema.cinema_backend.repository.PaymentCardRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class LegacyPaymentCardEncryptionRunner implements ApplicationRunner {

    private final PaymentCardRepository paymentCardRepository;
    private final CardEncryptionService cardEncryptionService;

    public LegacyPaymentCardEncryptionRunner(
            PaymentCardRepository paymentCardRepository,
            CardEncryptionService cardEncryptionService
    ) {
        this.paymentCardRepository = paymentCardRepository;
        this.cardEncryptionService = cardEncryptionService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<PaymentCard> legacyCards = paymentCardRepository.findAll().stream()
                .filter(card -> !cardEncryptionService.isEncrypted(card.getStoredCardNumber()))
                .toList();
        if (legacyCards.isEmpty()) {
            return;
        }

        for (PaymentCard card : legacyCards) {
            String originalNumber = card.getStoredCardNumber();
            String digits = originalNumber.replaceAll("\\D", "");
            card.replaceStoredCardNumber(cardEncryptionService.encrypt(originalNumber));
            card.setPresentationMetadata(
                    "Card",
                    digits.length() >= 4 ? digits.substring(digits.length() - 4) : null
            );
        }
        paymentCardRepository.saveAll(legacyCards);
    }
}
