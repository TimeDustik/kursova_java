package ua.edu.inventory.common;

/**
 * Константи типів сутностей для PermissionEvaluator та AuditLog.
 */
public final class EntityType {
    private EntityType() {}

    public static final String EQUIPMENT = "Equipment";
    public static final String LICENSE   = "License";
    public static final String SITE      = "Site";
    public static final String USER      = "User";
    public static final String AUDIT_LOG = "AuditLog";
}
