package ua.edu.inventory.license;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.edu.inventory.license.dto.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/licenses")
@RequiredArgsConstructor
@Tag(name = "Ліцензії", description = "Облік програмних ліцензій")
public class LicenseController {

    private final LicenseService licenseService;

    @Operation(summary = "Список ліцензій з фільтрами")
    @GetMapping
    public Page<LicenseDto> getAll(
            @RequestParam(required = false) UUID siteId,
            @RequestParam(required = false) LicenseType licenseType,
            @RequestParam(required = false) UUID assignedUserId,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return licenseService.getAll(new LicenseFilterDto(siteId, licenseType, assignedUserId, q), pageable);
    }

    @Operation(summary = "Отримати ліцензію за id")
    @GetMapping("/{id}")
    public LicenseDto getById(@PathVariable UUID id) {
        return licenseService.getById(id);
    }

    @Operation(summary = "Додати ліцензію")
    @PostMapping
    public ResponseEntity<LicenseDto> create(@Valid @RequestBody LicenseCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(licenseService.create(dto));
    }

    @Operation(summary = "Оновити ліцензію")
    @PutMapping("/{id}")
    public LicenseDto update(@PathVariable UUID id, @Valid @RequestBody LicenseUpdateDto dto) {
        return licenseService.update(id, dto);
    }

    @Operation(summary = "Видалити ліцензію")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        licenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Призначити ліцензію користувачеві")
    @PostMapping("/{id}/assign")
    public LicenseDto assign(@PathVariable UUID id, @Valid @RequestBody AssignRequest request) {
        return licenseService.assign(id, request.userId());
    }

    @Operation(summary = "Зняти призначення ліцензії")
    @PostMapping("/{id}/unassign")
    public LicenseDto unassign(@PathVariable UUID id) {
        return licenseService.unassign(id);
    }
}
