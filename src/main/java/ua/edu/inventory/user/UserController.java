package ua.edu.inventory.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.edu.inventory.user.dto.UserCreateDto;
import ua.edu.inventory.user.dto.UserDto;
import ua.edu.inventory.user.dto.UserUpdateDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Користувачі", description = "Управління користувачами (тільки ADMIN)")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Список усіх користувачів (ADMIN/AUDITOR)")
    @GetMapping
    public Page<UserDto> getAll(Pageable pageable) {
        return userService.getAll(pageable);
    }

    @Operation(summary = "Список користувачів сайту")
    @GetMapping("/by-site/{siteId}")
    public ResponseEntity<?> getBySite(@PathVariable UUID siteId) {
        return ResponseEntity.ok(userService.getBySite(siteId));
    }

    @Operation(summary = "Отримати користувача за id")
    @GetMapping("/{id}")
    public UserDto getById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    @Operation(summary = "Створити користувача (ADMIN)")
    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    @Operation(summary = "Оновити користувача (ADMIN)")
    @PutMapping("/{id}")
    public UserDto update(@PathVariable UUID id, @Valid @RequestBody UserUpdateDto dto) {
        return userService.update(id, dto);
    }

    @Operation(summary = "Деактивувати користувача (ADMIN, soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
