package ua.edu.inventory.report;

import java.util.UUID;

/** DTO, що серіалізується у JSON і відправляється у чергу inventory.reports. */
public record ReportMessage(
        UUID reportId,
        String reportType,   // "INVENTORY" | "LICENSES"
        UUID requestedByUserId,
        UUID siteId,
        String filename
) {}
