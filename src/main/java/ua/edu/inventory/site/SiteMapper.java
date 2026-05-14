package ua.edu.inventory.site;

import org.mapstruct.*;
import ua.edu.inventory.site.dto.SiteCreateDto;
import ua.edu.inventory.site.dto.SiteDto;
import ua.edu.inventory.site.dto.SiteUpdateDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SiteMapper {

    SiteDto toDto(Site site);

    @Mapping(target = "id", ignore = true)
    Site toEntity(SiteCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(SiteUpdateDto dto, @MappingTarget Site site);
}
