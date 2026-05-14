package ua.edu.inventory.user;

import jakarta.persistence.*;
import lombok.*;
import ua.edu.inventory.common.BaseAuditableEntity;

import java.util.UUID;

/**
 * Сутність користувача системи.
 * Ролі: WORKER (прив'язаний до Site), TEAM_LEAD (керівник Site),
 * ADMIN (повний доступ), AUDITOR (тільки читання всього).
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uq_users_email",    columnNames = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Column(length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "user_role")
    private UserRole role;

    /** null для ADMIN та AUDITOR */
    @Column(name = "site_id")
    private UUID siteId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
