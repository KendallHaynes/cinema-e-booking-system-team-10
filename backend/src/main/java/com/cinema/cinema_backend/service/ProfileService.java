package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.controller.AddressRequest;
import com.cinema.cinema_backend.controller.ChangePasswordRequest;
import com.cinema.cinema_backend.controller.PaymentCardRequest;
import com.cinema.cinema_backend.controller.PaymentCardResponse;
import com.cinema.cinema_backend.controller.ProfileResponse;
import com.cinema.cinema_backend.controller.ProfileUpdateRequest;
import com.cinema.cinema_backend.model.PaymentCard;
import com.cinema.cinema_backend.model.ProfileAddress;
import com.cinema.cinema_backend.model.User;
import com.cinema.cinema_backend.repository.PaymentCardRepository;
import com.cinema.cinema_backend.repository.ProfileAddressRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;

@Service
public class ProfileService {

    private static final long MAX_PAYMENT_CARDS = 3;

    private final UserRepository userRepository;
    private final ProfileAddressRepository addressRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final PasswordEncoder passwordEncoder;
    private final CardEncryptionService cardEncryptionService;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public ProfileService(
            UserRepository userRepository,
            ProfileAddressRepository addressRepository,
            PaymentCardRepository paymentCardRepository,
            PasswordEncoder passwordEncoder,
            CardEncryptionService cardEncryptionService,
            JavaMailSender mailSender,
            @Value("${app.mail-from}") String fromAddress
    ) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.paymentCardRepository = paymentCardRepository;
        this.passwordEncoder = passwordEncoder;
        this.cardEncryptionService = cardEncryptionService;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        User user = getUser(userId);
        ProfileAddress address = addressRepository.findByUser_UserId(userId).orElse(null);
        List<PaymentCardResponse> cards = paymentCardRepository.findAllByUser_UserIdOrderByCardId(userId)
                .stream()
                .map(PaymentCardResponse::from)
                .toList();
        return new ProfileResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                address == null ? null : com.cinema.cinema_backend.controller.AddressResponse.from(address),
                cards
        );
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = getUserForUpdate(userId);
        ProfileAddress address = addressRepository.findByUser_UserId(userId).orElse(null);
        boolean changed = !user.getFirstName().equals(request.firstName().trim())
                || !user.getLastName().equals(request.lastName().trim());

        user.updateProfile(request.firstName().trim(), request.lastName().trim());
        userRepository.save(user);

        AddressRequest addressRequest = request.address();
        if (addressRequest != null) {
            if (address == null) {
                address = new ProfileAddress(
                        user,
                        addressRequest.street().trim(),
                        addressRequest.city().trim(),
                        addressRequest.region().trim(),
                        addressRequest.postalCode().trim(),
                        addressRequest.country().trim()
                );
                addressRepository.save(address);
                changed = true;
            } else if (addressChanged(address, addressRequest)) {
                address.update(
                        addressRequest.street().trim(),
                        addressRequest.city().trim(),
                        addressRequest.region().trim(),
                        addressRequest.postalCode().trim(),
                        addressRequest.country().trim()
                );
                addressRepository.save(address);
                changed = true;
            }
            if (changed) {
                String billingAddress = formatAddress(address);
                paymentCardRepository.findAllByUser_UserIdOrderByCardId(userId)
                        .forEach(card -> card.updateBillingAddress(billingAddress));
            }
        }

        if (changed) {
            sendProfileChangeNotification(user);
        }
        return getProfile(userId);
    }

    @Transactional
    public PaymentCardResponse addPaymentCard(Long userId, PaymentCardRequest request) {
        User user = getUserForUpdate(userId);
        if (paymentCardRepository.countByUser_UserId(userId) >= MAX_PAYMENT_CARDS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You can save up to three payment cards.");
        }
        ProfileAddress address = addressRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Save your billing address in your profile before adding a payment card."
                ));

        String digits = request.cardNumber().replaceAll("[ -]", "");
        if (!digits.matches("\\d{13,19}") || !passesLuhn(digits)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid payment-card number.");
        }
        YearMonth expiry = YearMonth.of(request.expirationYear(), request.expirationMonth());
        if (expiry.isBefore(YearMonth.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The payment card has expired.");
        }

        String billingAddress = String.join(", ",
                address.getStreet(),
                address.getCity(),
                address.getRegion(),
                address.getPostalCode(),
                address.getCountry()
        );
        PaymentCard card = paymentCardRepository.save(new PaymentCard(
                user,
                cardEncryptionService.encrypt(digits),
                expiry.atDay(1),
                billingAddress,
                getCardBrand(digits),
                digits.substring(digits.length() - 4)
        ));
        return PaymentCardResponse.from(card);
    }

    @Transactional
    public void removePaymentCard(Long userId, Long cardId) {
        PaymentCard card = paymentCardRepository.findByCardIdAndUser_UserId(cardId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment card not found."));
        if (paymentCardRepository.countBookingsByCardId(cardId) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This card is linked to an existing booking and cannot be removed."
            );
        }
        paymentCardRepository.delete(card);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUserForUpdate(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your current password is incorrect.");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new passwords do not match.");
        }
        if (request.newPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be 10 to 72 characters and include an uppercase letter, a lowercase letter, a number, and a symbol.");
        }
        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User getUser(Long userId) {
        return validateActiveAccount(userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again.")));
    }

    private User getUserForUpdate(Long userId) {
        return validateActiveAccount(userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again.")));
    }

    private static User validateActiveAccount(User user) {
        if (!user.canSignIn()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account cannot access profile services.");
        }
        return user;
    }

    private void sendProfileChangeNotification(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("Your Cinema E-Booking profile was updated");
        message.setText("""
                Hello %s,

                Your personal profile information was changed. If you did not make this change, please contact support and change your password.

                For your security, this email does not include your profile or payment details.
                """.formatted(user.getFirstName()));
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The profile update could not be completed because its security notification could not be sent."
            );
        }
    }

    private static boolean addressChanged(ProfileAddress current, AddressRequest proposed) {
        return !current.getStreet().equals(proposed.street().trim())
                || !current.getCity().equals(proposed.city().trim())
                || !current.getRegion().equals(proposed.region().trim())
                || !current.getPostalCode().equals(proposed.postalCode().trim())
                || !current.getCountry().equals(proposed.country().trim());
    }

    private static String formatAddress(ProfileAddress address) {
        return String.join(", ",
                address.getStreet(),
                address.getCity(),
                address.getRegion(),
                address.getPostalCode(),
                address.getCountry()
        );
    }

    private static boolean passesLuhn(String digits) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int index = digits.length() - 1; index >= 0; index--) {
            int digit = digits.charAt(index) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }

    private static String getCardBrand(String digits) {
        if (digits.startsWith("4")) {
            return "Visa";
        }
        if (digits.startsWith("34") || digits.startsWith("37")) {
            return "American Express";
        }
        if (digits.matches("^(5[1-5]|2(2[2-9]|[3-6]\\d|7[01]|720)).*")) {
            return "Mastercard";
        }
        if (digits.startsWith("6011") || digits.startsWith("65")) {
            return "Discover";
        }
        return "Card";
    }
}
