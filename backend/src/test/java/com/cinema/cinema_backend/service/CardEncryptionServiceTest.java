package com.cinema.cinema_backend.service;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardEncryptionServiceTest {

    private static final byte[] KEY = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
    private final CardEncryptionService encryptionService =
            new CardEncryptionService(Base64.getEncoder().encodeToString(KEY));

    @Test
    void encryptsCardNumberWithAuthenticatedEncryptionAndFreshNonce() throws Exception {
        String cardNumber = "4111111111111111";

        String firstCiphertext = encryptionService.encrypt(cardNumber);
        String secondCiphertext = encryptionService.encrypt(cardNumber);

        assertTrue(encryptionService.isEncrypted(firstCiphertext));
        assertNotEquals(cardNumber, firstCiphertext);
        assertNotEquals(firstCiphertext, secondCiphertext);
        assertEquals(cardNumber, decrypt(firstCiphertext));
    }

    private String decrypt(String storedValue) throws Exception {
        byte[] encrypted = Base64.getDecoder().decode(storedValue.substring("enc:v1:".length()));
        byte[] nonce = Arrays.copyOfRange(encrypted, 0, 12);
        byte[] payload = Arrays.copyOfRange(encrypted, 12, encrypted.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
                Cipher.DECRYPT_MODE,
                new SecretKeySpec(KEY, "AES"),
                new GCMParameterSpec(128, nonce)
        );
        return new String(cipher.doFinal(payload), StandardCharsets.UTF_8);
    }
}
