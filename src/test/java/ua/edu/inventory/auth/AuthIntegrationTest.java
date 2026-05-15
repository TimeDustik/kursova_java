package ua.edu.inventory.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.edu.inventory.AbstractIntegrationTest;
import ua.edu.inventory.auth.dto.AuthResponse;
import ua.edu.inventory.auth.dto.LoginRequest;
import ua.edu.inventory.auth.dto.RefreshRequest;
import ua.edu.inventory.user.User;
import ua.edu.inventory.user.UserRepository;
import ua.edu.inventory.user.UserRole;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired UserRepository   userRepository;
    @Autowired PasswordEncoder  passwordEncoder;

    private static final String TEST_USERNAME = "integration_admin";
    private static final String TEST_PASSWORD = "TestPass123!";

    @BeforeEach
    void seedUser() {
        if (userRepository.findByUsername(TEST_USERNAME).isEmpty()) {
            User user = User.builder()
                    .username(TEST_USERNAME)
                    .email("integration_admin@test.com")
                    .fullName("Integration Admin")
                    .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                    .role(UserRole.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(user);
        }
    }

    @Test
    void login_withValidCredentials_returnsTokens() {
        var request = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).isNotBlank();
    }

    @Test
    void login_withWrongPassword_returns401() {
        var request = new LoginRequest(TEST_USERNAME, "wrong-password");
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refresh_withValidToken_returnsNewTokens() {
        // First login
        var loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        var loginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, AuthResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String refreshToken = loginResponse.getBody().refreshToken();

        // Refresh
        var refreshRequest = new RefreshRequest(refreshToken);
        ResponseEntity<AuthResponse> refreshResponse = restTemplate.postForEntity(
                "/api/v1/auth/refresh", refreshRequest, AuthResponse.class);

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResponse.getBody().accessToken()).isNotBlank();
        // New refresh token should be different (rotation)
        assertThat(refreshResponse.getBody().refreshToken()).isNotEqualTo(refreshToken);
    }

    @Test
    void refresh_withInvalidToken_returns401() {
        var request = new RefreshRequest(UUID.randomUUID().toString());
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/refresh", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/equipment", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedEndpoint_withValidToken_returns200() {
        var loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        var loginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, AuthResponse.class);
        String accessToken = loginResponse.getBody().accessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        var entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/equipment?page=0&size=10", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
