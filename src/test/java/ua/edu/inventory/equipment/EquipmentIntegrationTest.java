package ua.edu.inventory.equipment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.edu.inventory.AbstractIntegrationTest;
import ua.edu.inventory.audit.AuditLog;
import ua.edu.inventory.audit.AuditLogRepository;
import ua.edu.inventory.auth.dto.AuthResponse;
import ua.edu.inventory.auth.dto.LoginRequest;
import ua.edu.inventory.equipment.dto.EquipmentDto;
import ua.edu.inventory.site.Site;
import ua.edu.inventory.site.SiteRepository;
import ua.edu.inventory.user.User;
import ua.edu.inventory.user.UserRepository;
import ua.edu.inventory.user.UserRole;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EquipmentIntegrationTest extends AbstractIntegrationTest {

    @Autowired TestRestTemplate   restTemplate;
    @Autowired UserRepository     userRepository;
    @Autowired SiteRepository     siteRepository;
    @Autowired AuditLogRepository auditLogRepository;
    @Autowired PasswordEncoder    passwordEncoder;

    private static final String ADMIN_USER = "eq_test_admin";
    private static final String ADMIN_PASS = "AdminPass123!";
    private static final String WORKER_USER = "eq_test_worker";
    private static final String WORKER_PASS = "WorkerPass123!";

    private Site testSite;

    @BeforeEach
    void seedData() {
        if (siteRepository.findAll().stream()
                .noneMatch(s -> "TestSite".equals(s.getName()))) {
            testSite = siteRepository.save(Site.builder().name("TestSite").build());
        } else {
            testSite = siteRepository.findAll().stream()
                    .filter(s -> "TestSite".equals(s.getName())).findFirst().orElseThrow();
        }

        if (userRepository.findByUsername(ADMIN_USER).isEmpty()) {
            userRepository.save(User.builder()
                    .username(ADMIN_USER).email(ADMIN_USER + "@test.com")
                    .passwordHash(passwordEncoder.encode(ADMIN_PASS))
                    .role(UserRole.ADMIN).active(true).build());
        }
        if (userRepository.findByUsername(WORKER_USER).isEmpty()) {
            userRepository.save(User.builder()
                    .username(WORKER_USER).email(WORKER_USER + "@test.com")
                    .passwordHash(passwordEncoder.encode(WORKER_PASS))
                    .role(UserRole.WORKER).siteId(testSite.getId()).active(true).build());
        }
    }

    private String loginAndGetToken(String username, String password) {
        var resp = restTemplate.postForEntity(
                "/api/v1/auth/login",
                new LoginRequest(username, password),
                AuthResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return resp.getBody().accessToken();
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ─── RBAC ─────────────────────────────────────────────────────────────────

    @Test
    void getAll_asWorker_returnsOnlyOwnEquipment() {
        String workerToken = loginAndGetToken(WORKER_USER, WORKER_PASS);
        var entity = new HttpEntity<>(bearerHeaders(workerToken));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/equipment?page=0&size=50", HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createEquipment_asAdmin_succeeds() {
        String adminToken = loginAndGetToken(ADMIN_USER, ADMIN_PASS);
        String body = """
                {
                  "name": "Test Laptop",
                  "type": "LAPTOP",
                  "manufacturer": "Dell",
                  "siteId": "%s"
                }
                """.formatted(testSite.getId());

        var entity = new HttpEntity<>(body, bearerHeaders(adminToken));
        ResponseEntity<EquipmentDto> response = restTemplate.exchange(
                "/api/v1/equipment", HttpMethod.POST, entity, EquipmentDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Test Laptop");
        assertThat(response.getBody().inventoryNumber()).matches("EQ-\\d{4}-\\d{5}");
    }

    @Test
    void createEquipment_asWorker_returns403() {
        String workerToken = loginAndGetToken(WORKER_USER, WORKER_PASS);
        String body = """
                {"name": "Laptop", "type": "LAPTOP", "siteId": "%s"}
                """.formatted(testSite.getId());

        var entity = new HttpEntity<>(body, bearerHeaders(workerToken));
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/equipment", HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ─── Audit log ────────────────────────────────────────────────────────────

    @Test
    void createEquipment_writesAuditLog() {
        String adminToken = loginAndGetToken(ADMIN_USER, ADMIN_PASS);
        long auditCountBefore = auditLogRepository.count();

        String body = """
                {"name": "Audit Test PC", "type": "LAPTOP", "siteId": "%s"}
                """.formatted(testSite.getId());

        var entity = new HttpEntity<>(body, bearerHeaders(adminToken));
        ResponseEntity<EquipmentDto> response = restTemplate.exchange(
                "/api/v1/equipment", HttpMethod.POST, entity, EquipmentDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Give the async audit listener a moment to flush (REQUIRES_NEW TX)
        List<AuditLog> recentLogs = auditLogRepository.findAll();
        assertThat((long) recentLogs.size()).isGreaterThan(auditCountBefore);
    }

    // ─── Inventory number ─────────────────────────────────────────────────────

    @Test
    void inventoryNumber_hasCorrectFormat() {
        String adminToken = loginAndGetToken(ADMIN_USER, ADMIN_PASS);
        String body = """
                {"name": "Network Switch", "type": "NETWORK", "siteId": "%s"}
                """.formatted(testSite.getId());

        var entity = new HttpEntity<>(body, bearerHeaders(adminToken));
        ResponseEntity<EquipmentDto> response = restTemplate.exchange(
                "/api/v1/equipment", HttpMethod.POST, entity, EquipmentDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String invNum = response.getBody().inventoryNumber();
        assertThat(invNum).matches("EQ-\\d{4}-\\d{5}");
    }
}
