package ua.edu.inventory.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.edu.inventory.auth.UserPrincipal;

import java.util.Optional;
import java.util.UUID;

/** Утиліти для отримання поточного аутентифікованого користувача. */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<UserPrincipal> getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static UUID getCurrentUserId() {
        return getCurrentPrincipal()
                .map(UserPrincipal::getId)
                .orElseThrow(() -> new IllegalStateException("Користувач не аутентифікований"));
    }

    public static UUID getCurrentUserSiteId() {
        return getCurrentPrincipal()
                .map(UserPrincipal::getSiteId)
                .orElseThrow(() -> new IllegalStateException("Користувач не аутентифікований"));
    }
}
