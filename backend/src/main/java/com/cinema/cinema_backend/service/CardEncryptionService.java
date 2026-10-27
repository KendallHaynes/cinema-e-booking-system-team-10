package com.cinema.cinema_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class CardEncryptionService {

    private static final String ENCRYPTION_PREFIX = "enc:v1:";
    private static final int NONCE_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String encodedKey;

    public CardEncryptionService(@Value("${app.card-encryption-key:}") String encodedKey) {
        this.encodedKey = encodedKey;
    }

    public String encrypt(String cardNumber) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Payment-card encryption is not configured. Set PAYMENT_CARD_ENCRYPTION_KEY to a base64-encoded 32-byte key.",
                    exception
            );
        }
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "PAYMENT_CARD_ENCRYPTION_KEY must decode to exactly 32 bytes."
            );
        }

        byte[] nonce = new byte[NONCE_LENGTH];
        RANDOM.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(keyBytes, "AES"),
                    new GCMParameterSpec(TAG_LENGTH_BITS, nonce)
            );
            byte[] encrypted = cipher.doFinal(cardNumber.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] result = Arrays.copyOf(nonce, nonce.length + encrypted.length);
            System.arraycopy(encrypted, 0, result, nonce.length, encrypted.length);
            return ENCRYPTION_PREFIX + Base64.getEncoder().encodeToString(result);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not encrypt payment-card information.", exception);
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }

    public boolean isEncrypted(String storedCardNumber) {
        return storedCardNumber != null && storedCardNumber.startsWith(ENCRYPTION_PREFIX);
    }
}
