package ua.edu.inventory.license;

import org.mapstruct.*;
import ua.edu.inventory.license.dto.LicenseCreateDto;
import ua.edu.inventory.license.dto.LicenseDto;
import ua.edu.inventory.license.dto.LicenseUpdateDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LicenseMapper {

    LicenseDto toDto(License license);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seatsUsed", constant = "0")
    @Mapping(target = "assignedUserId", ignore = true)
    License toEntity(LicenseCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "siteId", ignore = true)
    @Mapping(target = "licenseType", ignore = true)
    @Mapping(target = "seatsUsed", ignore = true)
    @Mapping(target = "assignedUserId", ignore = true)
    void updateEntity(LicenseUpdateDto dto, @MappingTarget License license);
}
