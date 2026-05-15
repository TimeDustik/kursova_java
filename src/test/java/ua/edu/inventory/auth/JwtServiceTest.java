package ua.edu.inventory.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.edu.inventory.config.JwtProperties;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // 64 bytes of zeros, Base64-encoded — valid HS512 key
    private static final String SECRET =
            "QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQQ==";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setAccessTokenTtlSeconds(900L);
        props.setRefreshTokenTtlSeconds(604800L);
        jwtService = new JwtService(props);
    }

    private UserPrincipal buildPrincipal(String role) {
        return UserPrincipal.builder()
                .id(UUID.randomUUID())
                .username("test_user")
                .password("hash")
                .email("test@test.com")
                .role(role)
                .siteId(UUID.randomUUID())
                .active(true)
                .build();
    }

    @Test
    void generateAndValidate_happyPath() {
        UserPrincipal principal = buildPrincipal("ADMIN");
        String token = jwtService.generateAccessToken(principal);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(principal.getId());
    }

    @Test
    void extractClaims_containsRoleAndUsername() {
        UserPrincipal principal = buildPrincipal("TEAM_LEAD");
        String token = jwtService.generateAccessToken(principal);

        var claims = jwtService.extractClaims(token);
        assertThat(claims.get("role", String.class)).isEqualTo("TEAM_LEAD");
        assertThat(claims.get("username", String.class)).isEqualTo("test_user");
    }

    @Test
    void extractClaims_containsSiteId() {
        UserPrincipal principal = buildPrincipal("WORKER");
        String token = jwtService.generateAccessToken(principal);

        var claims = jwtService.extractClaims(token);
        assertThat(claims.get("siteId", String.class))
                .isEqualTo(principal.getSiteId().toString());
    }

    @Test
    void isValid_returnsFalse_forTamperedToken() {
        UserPrincipal principal = buildPrincipal("ADMIN");
        String token = jwtService.generateAccessToken(principal);
        String tampered = token + "tampered";

        assertThat(jwtService.isValid(tampered)).isFalse();
    }

    @Test
    void isValid_returnsFalse_forRandomString() {
        assertThat(jwtService.isValid("not.a.jwt")).isFalse();
    }

    @Test
    void generateToken_withNullSiteId_succeeds() {
        UserPrincipal principal = UserPrincipal.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .password("hash")
                .email("admin@test.com")
                .role("ADMIN")
                .siteId(null)
                .active(true)
                .build();

        String token = jwtService.generateAccessToken(principal);
        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractClaims(token).get("siteId")).isNull();
    }
}
