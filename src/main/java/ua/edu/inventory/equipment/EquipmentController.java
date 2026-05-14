package ua.edu.inventory.equipment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.edu.inventory.equipment.dto.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
@Tag(name = "Обладнання", description = "Облік та управління обладнанням")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @Operation(summary = "Список обладнання з фільтрами та пагінацією")
    @GetMapping
    public Page<EquipmentDto> getAll(
            @RequestParam(required = false) UUID siteId,
            @RequestParam(required = false) EquipmentStatus status,
            @RequestParam(required = false) EquipmentType type,
            @RequestParam(required = false) UUID assignedUserId,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return equipmentService.getAll(
                new EquipmentFilterDto(siteId, status, type, assignedUserId, q), pageable);
    }

    @Operation(summary = "Отримати обладнання за id")
    @GetMapping("/{id}")
    public EquipmentDto getById(@PathVariable UUID id) {
        return equipmentService.getById(id);
    }

    @Operation(summary = "Додати обладнання (ADMIN або TEAM_LEAD свого Site)")
    @PostMapping
    public ResponseEntity<EquipmentDto> create(@Valid @RequestBody EquipmentCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.create(dto));
    }

    @Operation(summary = "Оновити обладнання")
    @PutMapping("/{id}")
    public EquipmentDto update(@PathVariable UUID id, @Valid @RequestBody EquipmentUpdateDto dto) {
        return equipmentService.update(id, dto);
    }

    @Operation(summary = "Видалити обладнання")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Призначити обладнання користувачеві")
    @PostMapping("/{id}/assign")
    public EquipmentDto assign(@PathVariable UUID id, @Valid @RequestBody AssignRequest request) {
        return equipmentService.assign(id, request.userId());
    }

    @Operation(summary = "Зняти призначення з обладнання")
    @PostMapping("/{id}/unassign")
    public EquipmentDto unassign(@PathVariable UUID id) {
        return equipmentService.unassign(id);
    }
}
