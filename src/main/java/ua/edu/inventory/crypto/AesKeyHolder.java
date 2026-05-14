package ua.edu.inventory.crypto;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Ін'єктує AES-ключ з application.yml у статичне поле LicenseKeyAttributeConverter.
 * JPA AttributeConverter не може використовувати @Autowired, тому використовуємо
 * цей компонент для ініціалізації через @PostConstruct.
 */
@Component
public class AesKeyHolder {

    @Value("${crypto.aes-key}")
    private String aesKey;

    @PostConstruct
    public void init() {
        LicenseKeyAttributeConverter.initKey(aesKey);
    }
}
