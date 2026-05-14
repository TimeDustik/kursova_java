package ua.edu.inventory.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.edu.inventory.config.JwtProperties;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Сервіс для генерації та валідації JWT access-токенів.
 * Використовує JJWT 0.12.x API (Jwts.parser().verifyWith(...)).
 * Pattern: Singleton — один екземпляр на весь застосунок.
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTtlMillis;

    public JwtService(JwtProperties props) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.getSecret()));
        this.accessTtlMillis = props.getAccessTokenTtlSeconds() * 1000L;
    }

    public String generateAccessToken(UserPrincipal principal) {
        Date now = new Date();
        return Jwts.builder()
                .subject(principal.getId().toString())
                .claim("username", principal.getUsername())
                .claim("role", principal.getRole())
                .claim("siteId", principal.getSiteId() != null ? principal.getSiteId().toString() : null)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTtlMillis))
                .signWith(signingKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Невалідний JWT: {}", e.getMessage());
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).getSubject());
    }
}
