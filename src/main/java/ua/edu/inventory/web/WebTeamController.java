package ua.edu.inventory.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ua.edu.inventory.auth.UserPrincipal;
import ua.edu.inventory.common.exception.ResourceNotFoundException;
import ua.edu.inventory.equipment.EquipmentRepository;
import ua.edu.inventory.equipment.EquipmentMapper;
import ua.edu.inventory.license.LicenseRepository;
import ua.edu.inventory.license.LicenseMapper;
import ua.edu.inventory.user.UserRepository;
import ua.edu.inventory.user.UserMapper;

import java.util.UUID;

@Controller
@RequestMapping("/team")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEAM_LEAD')")
public class WebTeamController {

    private final UserRepository       userRepository;
    private final UserMapper           userMapper;
    private final EquipmentRepository  equipmentRepository;
    private final EquipmentMapper      equipmentMapper;
    private final LicenseRepository    licenseRepository;
    private final LicenseMapper        licenseMapper;

    @GetMapping
    public String team(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        UUID siteId = principal.getSiteId();
        var members = userRepository.findAllBySiteIdAndActiveTrue(siteId)
                .stream().map(userMapper::toDto).toList();

        model.addAttribute("members",    members);
        model.addAttribute("activePage", "team");
        return "team/index";
    }

    @GetMapping("/{userId}")
    public String memberDetail(@PathVariable UUID userId,
                               @AuthenticationPrincipal UserPrincipal principal,
                               Model model) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Користувач", userId));

        // Team Lead can only see their own site members
        if (!user.getSiteId().equals(principal.getSiteId())) {
            return "redirect:/team";
        }

        var equipment = equipmentRepository.findAllByAssignedUserId(userId)
                .stream().map(equipmentMapper::toDto).toList();
        var licenses  = licenseRepository.findAllByAssignedUserId(userId)
                .stream().map(licenseMapper::toDto).toList();

        model.addAttribute("member",     userMapper.toDto(user));
        model.addAttribute("equipment",  equipment);
        model.addAttribute("licenses",   licenses);
        model.addAttribute("activePage", "team");
        return "team/user-detail";
    }
}
