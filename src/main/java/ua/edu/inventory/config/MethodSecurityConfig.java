package ua.edu.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import ua.edu.inventory.common.InventoryPermissionEvaluator;

/**
 * Реєстрація кастомного PermissionEvaluator для @PreAuthorize(hasPermission(...)).
 *
 * Pattern: Strategy — InventoryPermissionEvaluator обирає стратегію перевірки прав
 * залежно від ролі та типу сутності.
 *
 * @EnableMethodSecurity оголошено в SecurityConfig.
 */
@Configuration
public class MethodSecurityConfig {

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            InventoryPermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
