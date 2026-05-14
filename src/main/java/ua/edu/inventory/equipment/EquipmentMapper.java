package ua.edu.inventory.equipment;

import org.mapstruct.*;
import ua.edu.inventory.equipment.dto.EquipmentDto;
import ua.edu.inventory.equipment.dto.EquipmentUpdateDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EquipmentMapper {

    EquipmentDto toDto(Equipment equipment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inventoryNumber", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "siteId", ignore = true)
    @Mapping(target = "assignedUserId", ignore = true)
    void updateEntity(EquipmentUpdateDto dto, @MappingTarget Equipment equipment);
}
