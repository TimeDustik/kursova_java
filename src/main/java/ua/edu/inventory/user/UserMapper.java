package ua.edu.inventory.user;

import org.mapstruct.*;
import ua.edu.inventory.user.dto.UserCreateDto;
import ua.edu.inventory.user.dto.UserDto;
import ua.edu.inventory.user.dto.UserUpdateDto;

/**
 * MapStruct-маппер для сутності User.
 * Пароль ніколи не потрапляє в DTO (явно виключено).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    User toEntity(UserCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "siteId", ignore = true)
    void updateEntity(UserUpdateDto dto, @MappingTarget User user);
}
