package ua.edu.inventory.report;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.edu.inventory.common.exception.ResourceNotFoundException;

import java.util.Map;
import java.util.UUID;

/**
 * REST-контролер для асинхронної генерації Excel-звітів.
 *
 * Протокол:
 *   1. GET /inventory.xlsx → 202 Accepted {reportId, statusUrl}
 *   2. GET /{id}/status    → {status, downloadUrl}  (polling)
 *   3. GET /{id}/download  → .xlsx файл
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Звіти", description = "Асинхронна генерація Excel-звітів через RabbitMQ")
public class ReportController {

    private static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReportProducer reportProducer;
    private final ReportStore reportStore;

    @Operation(summary = "Запит на звіт по обладнанню (202 Accepted, async)")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR') or hasRole('TEAM_LEAD')")
    @GetMapping("/inventory.xlsx")
    public ResponseEntity<Map<String, Object>> requestInventoryReport(
            @RequestParam(required = false) UUID siteId) {
        UUID reportId = reportProducer.requestInventoryReport(siteId);
        return accepted(reportId);
    }

    @Operation(summary = "Запит на звіт по ліцензіях (202 Accepted, async)")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR') or hasRole('TEAM_LEAD')")
    @GetMapping("/licenses.xlsx")
    public ResponseEntity<Map<String, Object>> requestLicenseReport(
            @RequestParam(required = false) UUID siteId) {
        UUID reportId = reportProducer.requestLicenseReport(siteId);
        return accepted(reportId);
    }

    @Operation(summary = "Статус генерації звіту")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{reportId}/status")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable UUID reportId) {
        ReportStore.Entry entry = reportStore.find(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Звіт", reportId));

        Map<String, Object> body = entry.isReady()
                ? Map.of("status", entry.getStatus(),
                         "downloadUrl", "/api/v1/reports/" + reportId + "/download",
                         "filename", entry.getFilename())
                : Map.of("status", entry.getStatus());

        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Завантажити готовий Excel-звіт")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{reportId}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID reportId) {
        ReportStore.Entry entry = reportStore.find(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Звіт", reportId));

        if (!entry.isReady()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(("Звіт ще не готовий. Статус: " + entry.getStatus()).getBytes());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + entry.getFilename() + "\"")
                .body(entry.getContent());
    }

    // ─── Допоміжні методи ─────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> accepted(UUID reportId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "reportId",  reportId,
                "statusUrl", "/api/v1/reports/" + reportId + "/status"
        ));
    }
}
