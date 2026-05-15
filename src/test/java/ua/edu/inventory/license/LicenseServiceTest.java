package ua.edu.inventory.license;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.edu.inventory.auth.UserPrincipal;
import ua.edu.inventory.common.exception.BusinessRuleException;
import ua.edu.inventory.license.dto.LicenseCreateDto;
import ua.edu.inventory.user.UserRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseServiceTest {

    @Mock LicenseRepository       licenseRepository;
    @Mock UserRepository          userRepository;
    @Mock LicenseMapper           licenseMapper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks LicenseService licenseService;

    private final UUID siteId = UUID.randomUUID();

    @BeforeEach
    void setSecurityContext() {
        var principal = UserPrincipal.builder()
                .id(UUID.randomUUID()).username("admin").role("ADMIN")
                .siteId(null).active(true).build();
        var auth = new UsernamePasswordAuthenticationToken(
                principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_throws_whenNonPerpetualWithoutExpiration() {
        var dto = new LicenseCreateDto("Office 365", "Microsoft", "KEY-123",
                LicenseType.SUBSCRIPTION, 10,
                null, null,          // no expirationDate
                null, siteId, null);

        assertThatThrownBy(() -> licenseService.create(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("дата закінчення");
    }

    @Test
    void create_succeeds_whenPerpetualWithoutExpiration() {
        var dto = new LicenseCreateDto("Windows 10", "Microsoft", "KEY-WIN10",
                LicenseType.PERPETUAL, 5,
                null, null,
                null, siteId, null);

        var license = License.builder().id(UUID.randomUUID()).softwareName("Windows 10").build();
        when(licenseRepository.save(any())).thenReturn(license);
        when(licenseMapper.toDto(any())).thenReturn(null);
        when(licenseMapper.toEntity(any())).thenReturn(license);

        licenseService.create(dto);

        verify(licenseRepository).save(any(License.class));
    }

    @Test
    void create_succeeds_whenSubscriptionWithExpiration() {
        var dto = new LicenseCreateDto("Jira", "Atlassian", "JIRA-KEY",
                LicenseType.SUBSCRIPTION, 50,
                null, java.time.LocalDate.now().plusYears(1),
                null, siteId, null);

        var license = License.builder().id(UUID.randomUUID()).softwareName("Jira").build();
        when(licenseRepository.save(any())).thenReturn(license);
        when(licenseMapper.toDto(any())).thenReturn(null);
        when(licenseMapper.toEntity(any())).thenReturn(license);

        licenseService.create(dto);

        verify(licenseRepository).save(any(License.class));
    }

    @Test
    void create_throws_whenConcurrentWithoutExpiration() {
        var dto = new LicenseCreateDto("Slack", "Salesforce", "SLACK-KEY",
                LicenseType.OEM, 20,
                null, null,
                null, siteId, null);

        assertThatThrownBy(() -> licenseService.create(dto))
                .isInstanceOf(BusinessRuleException.class);
    }
}
