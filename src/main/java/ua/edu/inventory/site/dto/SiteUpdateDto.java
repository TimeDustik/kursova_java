package ua.edu.inventory.site.dto;

import java.util.UUID;

public record SiteUpdateDto(String name, String address, String city, UUID teamLeadId) {}
