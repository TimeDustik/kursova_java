package ua.edu.inventory.common;

/**
 * Константи прав доступу для використання у @PreAuthorize з hasPermission().
 *
 * Приклад використання:
 *   @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'Equipment', 'READ')")
 */
public final class Permission {
    private Permission() {}

    /** Читання сутності або списку. */
    public static final String READ   = "READ";

    /** Зміна / оновлення сутності. */
    public static final String WRITE  = "WRITE";

    /** Призначення, зняття призначення (assign/unassign). */
    public static final String MANAGE = "MANAGE";
}
