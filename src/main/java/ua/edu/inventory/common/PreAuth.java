package ua.edu.inventory.common;

/**
 * Константи SpEL-виразів для @PreAuthorize.
 * Зменшує дублювання рядків у сервісах та спрощує тестування.
 *
 * Використання:
 *   @PreAuthorize(PreAuth.ADMIN_ONLY)
 *   @PreAuthorize(PreAuth.ADMIN_OR_AUDITOR)
 *   @PreAuthorize(PreAuth.ADMIN_OR_TEAM_LEAD)
 */
public final class PreAuth {
    private PreAuth() {}

    /** Тільки адміністратор. */
    public static final String ADMIN_ONLY =
            "hasRole('ADMIN')";

    /** Адміністратор або аудитор. */
    public static final String ADMIN_OR_AUDITOR =
            "hasAnyRole('ADMIN', 'AUDITOR')";

    /** Адміністратор або тимлід. */
    public static final String ADMIN_OR_TEAM_LEAD =
            "hasAnyRole('ADMIN', 'TEAM_LEAD')";

    /**
     * Тимлід (свій site) або адміністратор/аудитор.
     * #{entityType} і #{id} замінюються конкретними значеннями в анотації.
     */
    public static final String READ_SITE_ENTITY =
            "hasAnyRole('ADMIN','AUDITOR') or " +
            "(hasRole('TEAM_LEAD') and hasPermission(#id, #entityType, 'READ'))";

    /** Тимлід (свій site) або адміністратор. */
    public static final String WRITE_SITE_ENTITY =
            "hasRole('ADMIN') or " +
            "(hasRole('TEAM_LEAD') and hasPermission(#id, #entityType, 'WRITE'))";

    /** Доступ до Site: тимлід бачить лише свій, адмін/аудитор — всі. */
    public static final String ACCESS_SITE =
            "hasAnyRole('ADMIN','AUDITOR') or " +
            "(hasRole('TEAM_LEAD') and hasPermission(#siteId, 'Site', 'READ'))";
}
