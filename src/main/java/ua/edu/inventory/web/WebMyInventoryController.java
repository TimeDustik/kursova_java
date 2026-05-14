package ua.edu.inventory.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ua.edu.inventory.auth.UserPrincipal;
import ua.edu.inventory.equipment.EquipmentRepository;
import ua.edu.inventory.equipment.EquipmentMapper;
import ua.edu.inventory.license.LicenseRepository;
import ua.edu.inventory.license.LicenseMapper;

@Controller
@RequestMapping("/my-inventory")
@RequiredArgsConstructor
public class WebMyInventoryController {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper     equipmentMapper;
    private final LicenseRepository   licenseRepository;
    private final LicenseMapper       licenseMapper;

    @GetMapping
    public String myInventory(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        var equipment = equipmentRepository.findAllByAssignedUserId(principal.getId())
                .stream().map(equipmentMapper::toDto).toList();
        var licenses  = licenseRepository.findAllByAssignedUserId(principal.getId())
                .stream().map(licenseMapper::toDto).toList();

        model.addAttribute("equipment",  equipment);
        model.addAttribute("licenses",   licenses);
        model.addAttribute("activePage", "my-inventory");
        return "my-inventory";
    }
}
