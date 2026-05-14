package ua.edu.inventory.license.dto;

import ua.edu.inventory.license.LicenseType;

import java.util.UUID;

public record LicenseFilterDto(UUID siteId, LicenseType licenseType, UUID assignedUserId, String q) {}
