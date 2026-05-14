package ua.edu.inventory.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.edu.inventory.audit.AuditAction;
import ua.edu.inventory.audit.AuditLogService;
import ua.edu.inventory.equipment.EquipmentService;
import ua.edu.inventory.equipment.EquipmentStatus;
import ua.edu.inventory.equipment.EquipmentType;
import ua.edu.inventory.equipment.dto.EquipmentCreateDto;
import ua.edu.inventory.equipment.dto.EquipmentFilterDto;
import ua.edu.inventory.license.LicenseService;
import ua.edu.inventory.license.LicenseType;
import ua.edu.inventory.license.dto.LicenseCreateDto;
import ua.edu.inventory.license.dto.LicenseFilterDto;
import ua.edu.inventory.site.SiteService;
import ua.edu.inventory.site.dto.SiteCreateDto;
import ua.edu.inventory.user.UserRole;
import ua.edu.inventory.user.UserService;
import ua.edu.inventory.user.dto.UserCreateDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class WebAdminController {

    private final EquipmentService equipmentService;
    private final LicenseService   licenseService;
    private final UserService      userService;
    private final SiteService      siteService;
    private final AuditLogService  auditLogService;

    // ─── Equipment ────────────────────────────────────────────────────────────

    @GetMapping("/equipment")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR','TEAM_LEAD')")
    public String equipment(@RequestParam(defaultValue = "0")  int page,
                            @RequestParam(defaultValue = "20") int size,
                            @RequestParam(required = false) String q,
                            @RequestParam(required = false) EquipmentStatus status,
                            @RequestParam(required = false) EquipmentType type,
                            @RequestParam(required = false) UUID siteId,
                            Model model) {
        var filter   = new EquipmentFilterDto(siteId, status, type, null, q);
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var items    = equipmentService.getAll(filter, pageable);
        var sites    = siteService.getAll();

        model.addAttribute("page",       items);
        model.addAttribute("sites",      sites);
        model.addAttribute("statuses",   EquipmentStatus.values());
        model.addAttribute("types",      EquipmentType.values());
        model.addAttribute("filter",     filter);
        model.addAttribute("activePage", "equipment");
        return "admin/equipment";
    }

    @PostMapping("/equipment")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEAD')")
    public String createEquipment(@RequestParam String name,
                                   @RequestParam EquipmentType type,
                                   @RequestParam(required = false) String manufacturer,
                                   @RequestParam(required = false) String model,
                                   @RequestParam(required = false) String serialNumber,
                                   @RequestParam(required = false) String price,
                                   @RequestParam UUID siteId,
                                   @RequestParam(required = false) LocalDate purchaseDate,
                                   @RequestParam(required = false) String notes,
                                   RedirectAttributes ra) {
        try {
            BigDecimal priceVal = (price != null && !price.isBlank()) ? new BigDecimal(price) : null;
            var dto = new EquipmentCreateDto(name, type, manufacturer, model, serialNumber,
                    purchaseDate, null, priceVal, siteId, notes);
            equipmentService.create(dto);
            ra.addFlashAttribute("successMsg", "Обладнання додано успішно");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/equipment";
    }

    @PostMapping("/equipment/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEAD')")
    public String deleteEquipment(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            equipmentService.delete(id);
            ra.addFlashAttribute("successMsg", "Обладнання видалено");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/equipment";
    }

    // ─── Licenses ─────────────────────────────────────────────────────────────

    @GetMapping("/licenses")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR','TEAM_LEAD')")
    public String licenses(@RequestParam(defaultValue = "0")  int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(required = false) String q,
                           @RequestParam(required = false) LicenseType licenseType,
                           @RequestParam(required = false) UUID siteId,
                           Model model) {
        var filter   = new LicenseFilterDto(siteId, licenseType, null, q);
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var items    = licenseService.getAll(filter, pageable);
        var sites    = siteService.getAll();

        model.addAttribute("page",         items);
        model.addAttribute("sites",        sites);
        model.addAttribute("licenseTypes", LicenseType.values());
        model.addAttribute("filter",       filter);
        model.addAttribute("activePage",   "licenses");
        return "admin/licenses";
    }

    @PostMapping("/licenses")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEAD')")
    public String createLicense(@RequestParam String softwareName,
                                 @RequestParam String vendor,
                                 @RequestParam LicenseType licenseType,
                                 @RequestParam int seatsTotal,
                                 @RequestParam(required = false) LocalDate purchaseDate,
                                 @RequestParam(required = false) LocalDate expirationDate,
                                 @RequestParam(required = false) String cost,
                                 @RequestParam UUID siteId,
                                 @RequestParam(required = false) String licenseKey,
                                 RedirectAttributes ra) {
        try {
            BigDecimal costVal = (cost != null && !cost.isBlank()) ? new BigDecimal(cost) : null;
            var dto = new LicenseCreateDto(softwareName, vendor, licenseKey, licenseType, seatsTotal,
                    purchaseDate, expirationDate, costVal, siteId, null);
            licenseService.create(dto);
            ra.addFlashAttribute("successMsg", "Ліцензію додано успішно");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/licenses";
    }

    @PostMapping("/licenses/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEAD')")
    public String deleteLicense(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            licenseService.delete(id);
            ra.addFlashAttribute("successMsg", "Ліцензію видалено");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/licenses";
    }

    // ─── Users ────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public String users(@RequestParam(defaultValue = "0")  int page,
                        @RequestParam(defaultValue = "20") int size,
                        Model model) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var items    = userService.getAll(pageable);
        var sites    = siteService.getAll();

        model.addAttribute("page",       items);
        model.addAttribute("sites",      sites);
        model.addAttribute("roles",      UserRole.values());
        model.addAttribute("activePage", "users");
        return "admin/users";
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@RequestParam String username,
                              @RequestParam String email,
                              @RequestParam String fullName,
                              @RequestParam(required = false) String phone,
                              @RequestParam UserRole role,
                              @RequestParam(required = false) UUID siteId,
                              @RequestParam String password,
                              RedirectAttributes ra) {
        try {
            var dto = new UserCreateDto(username, email, password, fullName, phone, role, siteId);
            userService.create(dto);
            ra.addFlashAttribute("successMsg", "Користувача створено");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public String deactivateUser(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            userService.deactivate(id);
            ra.addFlashAttribute("successMsg", "Користувача деактивовано");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ─── Sites ────────────────────────────────────────────────────────────────

    @GetMapping("/sites")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public String sites(Model model) {
        model.addAttribute("sites",      siteService.getAll());
        model.addAttribute("activePage", "sites");
        return "admin/sites";
    }

    @PostMapping("/sites")
    @PreAuthorize("hasRole('ADMIN')")
    public String createSite(@RequestParam String name,
                              @RequestParam(required = false) String address,
                              @RequestParam(required = false) String city,
                              @RequestParam(required = false) UUID teamLeadId,
                              RedirectAttributes ra) {
        try {
            var dto = new SiteCreateDto(name, address, city, teamLeadId);
            siteService.create(dto);
            ra.addFlashAttribute("successMsg", "Об'єкт створено");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/sites";
    }

    @PostMapping("/sites/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteSite(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            siteService.delete(id);
            ra.addFlashAttribute("successMsg", "Об'єкт видалено");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/sites";
    }

    // ─── Audit Log ────────────────────────────────────────────────────────────

    @GetMapping("/audit-log")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR','TEAM_LEAD')")
    public String auditLog(@RequestParam(defaultValue = "0")  int page,
                           @RequestParam(defaultValue = "30") int size,
                           @RequestParam(required = false) UUID actorId,
                           @RequestParam(required = false) String entityType,
                           @RequestParam(required = false) AuditAction action,
                           Model model) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var items    = auditLogService.getAll(actorId, entityType, null, action,
                null, null, pageable);

        model.addAttribute("page",       items);
        model.addAttribute("actions",    AuditAction.values());
        model.addAttribute("activePage", "audit");
        return "admin/audit-log";
    }
}
