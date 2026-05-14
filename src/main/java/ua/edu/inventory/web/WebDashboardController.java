package ua.edu.inventory.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ua.edu.inventory.auth.UserPrincipal;
import ua.edu.inventory.equipment.EquipmentRepository;
import ua.edu.inventory.equipment.EquipmentStatus;
import ua.edu.inventory.license.LicenseRepository;
import ua.edu.inventory.site.SiteRepository;
import ua.edu.inventory.user.UserRepository;

@Controller
@RequiredArgsConstructor
public class WebDashboardController {

    private final EquipmentRepository equipmentRepository;
    private final LicenseRepository   licenseRepository;
    private final UserRepository       userRepository;
    private final SiteRepository       siteRepository;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        String role = principal.getRole();

        if ("WORKER".equals(role)) {
            // Worker: personal stats only
            long myEquipment = equipmentRepository
                    .findAllByAssignedUserId(principal.getId()).size();
            long myLicenses  = licenseRepository
                    .findAllByAssignedUserId(principal.getId()).size();
            model.addAttribute("myEquipment", myEquipment);
            model.addAttribute("myLicenses",  myLicenses);

        } else if ("TEAM_LEAD".equals(role) && principal.getSiteId() != null) {
            // Team Lead: site-scoped stats
            var siteId = principal.getSiteId();
            model.addAttribute("totalEquipment",    equipmentRepository.countBySiteId(siteId));
            model.addAttribute("assignedEquipment",
                    equipmentRepository.countBySiteIdAndStatus(siteId, EquipmentStatus.ASSIGNED));
            model.addAttribute("totalLicenses",     licenseRepository.countBySiteId(siteId));
            model.addAttribute("exhaustedLicenses",
                    licenseRepository.countExhaustedBySiteId(siteId));
            model.addAttribute("teamSize",
                    userRepository.findAllBySiteIdAndActiveTrue(siteId).size());

        } else {
            // Admin / Auditor: global stats
            model.addAttribute("totalEquipment",  equipmentRepository.countActive());
            model.addAttribute("totalLicenses",   licenseRepository.count());
            model.addAttribute("totalUsers",       userRepository.count());
            model.addAttribute("totalSites",       siteRepository.count());
            model.addAttribute("inStockCount",
                    equipmentRepository.countByStatus(EquipmentStatus.IN_STOCK));
            model.addAttribute("assignedCount",
                    equipmentRepository.countByStatus(EquipmentStatus.ASSIGNED));
        }

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("role", role);
        return "dashboard";
    }
}
