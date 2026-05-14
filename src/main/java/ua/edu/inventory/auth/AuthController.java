package ua.edu.inventory.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.edu.inventory.auth.dto.AuthResponse;
import ua.edu.inventory.auth.dto.LoginRequest;
import ua.edu.inventory.auth.dto.MeResponse;
import ua.edu.inventory.auth.dto.RefreshRequest;

/**
 * REST-контролер аутентифікації.
 * Бізнес-логіка делегується AuthService.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентифікація", description = "Логін, оновлення токенів, логаут")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Вхід у систему")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Оновлення access-токена через refresh-токен (ротація)")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @Operation(summary = "Вихід із системи (анулювання refresh-токена)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Інформація про поточного користувача")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.me(principal.getId()));
    }
}
