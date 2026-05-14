package ua.edu.inventory.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Pattern: Decorator.
 * Reason: прозоро «обгортає» значення поля licenseKey шифруванням/дешифруванням
 * при збереженні в БД та читанні з неї, не змінюючи бізнес-логіку.
 *
 * Алгоритм: AES/GCM/NoPadding (256-bit) — автентифіковане шифрування.
 * Формат у БД: Base64(IV_12_bytes || ciphertext || auth_tag_16_bytes)
 */
@Converter
@Slf4j
public class LicenseKeyAttributeConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    // Ключ ініціалізується через AesKeyHolder (@PostConstruct)
    private static volatile SecretKey secretKey;

    static void initKey(String rawKey) {
        if (rawKey == null || rawKey.length() != 32) {
            throw new IllegalArgumentException("AES_KEY повинен бути рівно 32 ASCII-символи (256 біт)");
        }
        secretKey = new SecretKeySpec(rawKey.getBytes(), "AES");
        log.info("AES ключ для шифрування ліцензійних ключів ініціалізовано");
    }

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) return null;
        try {
            SecretKey key = requireKey();
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());

            // Зберігаємо IV разом з шифротекстом
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Помилка шифрування ліцензійного ключа", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String encrypted) {
        if (encrypted == null) return null;
        try {
            SecretKey key = requireKey();
            byte[] combined = Base64.getDecoder().decode(encrypted);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            throw new IllegalStateException("Помилка дешифрування ліцензійного ключа", e);
        }
    }

    private SecretKey requireKey() {
        if (secretKey == null) {
            throw new IllegalStateException("AES ключ не ініціалізовано. AesKeyHolder не запущено.");
        }
        return secretKey;
    }
}
