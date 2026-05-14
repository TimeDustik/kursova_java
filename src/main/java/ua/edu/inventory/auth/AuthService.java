package ua.edu.inventory.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.inventory.auth.dto.AuthResponse;
import ua.edu.inventory.auth.dto.LoginRequest;
import ua.edu.inventory.auth.dto.MeResponse;
import ua.edu.inventory.common.exception.BusinessRuleException;
import ua.edu.inventory.common.exception.ResourceNotFoundException;
import ua.edu.inventory.config.JwtProperties;
import ua.edu.inventory.user.User;
import ua.edu.inventory.user.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервіс аутентифікації: логін, оновлення токенів, логаут.
 * Реалізує ротацію refresh-токенів (старий анулюється при кожному оновленні).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return buildAuthResponse(principal);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new BusinessRuleException("Refresh token не знайдено або вже використано"));

        if (!stored.isValid()) {
            throw new BusinessRuleException("Refresh token прострочений або анульований");
        }

        // Ротація: анулюємо старий токен
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", stored.getUserId()));

        if (!user.isActive()) {
            throw new BusinessRuleException("Обліковий запис деактивовано");
        }

        return buildAuthResponse(UserPrincipal.from(user));
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByToken(rawRefreshToken)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    @Transactional(readOnly = true)
    public MeResponse me(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return new MeResponse(
                user.getId(), user.getUsername(), user.getFullName(),
                user.getEmail(), user.getPhone(), user.getRole(),
                user.getSiteId(), user.isActive());
    }

    private AuthResponse buildAuthResponse(UserPrincipal principal) {
        String accessToken = jwtService.generateAccessToken(principal);
        String rawRefresh = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(principal.getId())
                .token(rawRefresh)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenTtlSeconds()))
                .build();
        refreshTokenRepository.save(refreshToken);

        log.info("Користувач {} успішно увійшов у систему", principal.getUsername());

        return new AuthResponse(
                accessToken,
                rawRefresh,
                new AuthResponse.UserInfo(
                        principal.getId(), principal.getUsername(),
                        principal.getFullName(), principal.getEmail(),
                        ua.edu.inventory.user.UserRole.valueOf(principal.getRole()),
                        principal.getSiteId())
        );
    }
}
