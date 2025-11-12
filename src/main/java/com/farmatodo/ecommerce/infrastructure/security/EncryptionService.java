package com.farmatodo.ecommerce.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKeySpec key;
    private final GCMParameterSpec iv;

    public EncryptionService(
            @Value("${security.encryption.aes-key}") String base64Key,
            @Value("${security.encryption.aes-iv}") String base64Iv
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        byte[] ivBytes = Base64.getDecoder().decode(base64Iv);

        this.key = new SecretKeySpec(keyBytes, "AES");
        this.iv = new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes);
    }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

}
