package ua.edu.inventory.site;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.edu.inventory.site.dto.SiteCreateDto;
import ua.edu.inventory.site.dto.SiteDto;
import ua.edu.inventory.site.dto.SiteUpdateDto;
import ua.edu.inventory.user.UserService;
import ua.edu.inventory.user.dto.UserDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sites")
@RequiredArgsConstructor
@Tag(name = "Об'єкти (Site)", description = "Управління філіалами/об'єктами")
public class SiteController {

    private final SiteService siteService;
    private final UserService userService;

    @Operation(summary = "Список об'єктів")
    @GetMapping
    public List<SiteDto> getAll() {
        return siteService.getAll();
    }

    @Operation(summary = "Отримати об'єкт за id")
    @GetMapping("/{id}")
    public SiteDto getById(@PathVariable UUID id) {
        return siteService.getById(id);
    }

    @Operation(summary = "Список працівників об'єкту")
    @GetMapping("/{id}/workers")
    public List<UserDto> getWorkers(@PathVariable UUID id) {
        return userService.getBySite(id);
    }

    @Operation(summary = "Створити об'єкт (ADMIN)")
    @PostMapping
    public ResponseEntity<SiteDto> create(@Valid @RequestBody SiteCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(siteService.create(dto));
    }

    @Operation(summary = "Оновити об'єкт (ADMIN)")
    @PutMapping("/{id}")
    public SiteDto update(@PathVariable UUID id, @Valid @RequestBody SiteUpdateDto dto) {
        return siteService.update(id, dto);
    }

    @Operation(summary = "Видалити об'єкт (ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        siteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
