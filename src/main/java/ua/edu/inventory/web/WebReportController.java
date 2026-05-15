package ua.edu.inventory.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.inventory.auth.UserPrincipal;
import ua.edu.inventory.report.ExcelReportGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class WebReportController {

    private static final String XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ExcelReportGenerator generator;

    @GetMapping("/equipment.xlsx")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR','TEAM_LEAD')")
    public ResponseEntity<byte[]> equipmentReport(
            @RequestParam(required = false) UUID siteId,
            @AuthenticationPrincipal UserPrincipal principal) throws IOException {

        UUID effectiveSite = resolvesite(siteId, principal);
        byte[] data = generator.generateInventoryReport(effectiveSite);
        String filename = "equipment-" + LocalDate.now() + ".xlsx";
        return download(data, filename);
    }

    @GetMapping("/licenses.xlsx")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR','TEAM_LEAD')")
    public ResponseEntity<byte[]> licenseReport(
            @RequestParam(required = false) UUID siteId,
            @AuthenticationPrincipal UserPrincipal principal) throws IOException {

        UUID effectiveSite = resolvesite(siteId, principal);
        byte[] data = generator.generateLicenseReport(effectiveSite);
        String filename = "licenses-" + LocalDate.now() + ".xlsx";
        return download(data, filename);
    }

    private UUID resolvesite(UUID requested, UserPrincipal principal) {
        String role = principal.getRole();
        if ("ADMIN".equals(role) || "AUDITOR".equals(role)) {
            return requested; // null = всі об'єкти
        }
        return principal.getSiteId(); // TEAM_LEAD бачить тільки свій сайт
    }

    private ResponseEntity<byte[]> download(byte[] data, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(data);
    }
}
