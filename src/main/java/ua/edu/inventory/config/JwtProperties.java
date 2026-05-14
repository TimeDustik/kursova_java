package ua.edu.inventory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Параметри JWT зчитуються з application.yml (префікс "jwt").
 * Pattern: Singleton — Spring створює один екземпляр цього біна.
 */
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private long accessTokenTtlSeconds = 900;
    private long refreshTokenTtlSeconds = 604800;
}
