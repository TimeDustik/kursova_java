package ua.edu.inventory.equipment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.edu.inventory.auth.UserPrincipal;
import ua.edu.inventory.common.exception.BusinessRuleException;
import ua.edu.inventory.common.exception.ConflictException;
import ua.edu.inventory.equipment.dto.EquipmentCreateDto;
import ua.edu.inventory.notification.NotificationProducer;
import ua.edu.inventory.user.User;
import ua.edu.inventory.user.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock EquipmentRepository equipmentRepository;
    @Mock UserRepository       userRepository;
    @Mock EquipmentMapper      equipmentMapper;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock NotificationProducer notificationProducer;

    @InjectMocks EquipmentService equipmentService;

    private final UUID siteId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setSecurityContext() {
        var principal = UserPrincipal.builder()
                .id(userId).username("admin").role("ADMIN")
                .siteId(null).active(true).build();
        var auth = new UsernamePasswordAuthenticationToken(
                principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    void create_succeeds_whenNoConflict() {
        var dto = new EquipmentCreateDto("Laptop", EquipmentType.LAPTOP,
                "Dell", "XPS", "SN-001", null, null, null, siteId, null);

        when(equipmentRepository.existsBySerialNumber("SN-001")).thenReturn(false);
        when(equipmentRepository.nextSeq()).thenReturn(1L);

        Equipment savedEq = Equipment.builder()
                .id(UUID.randomUUID()).name("Laptop")
                .inventoryNumber("EQ-2025-00001").build();
        when(equipmentRepository.save(any())).thenReturn(savedEq);
        when(equipmentMapper.toDto(any())).thenReturn(null);

        equipmentService.create(dto);

        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    void create_throws_whenSerialNumberDuplicate() {
        var dto = new EquipmentCreateDto("Laptop", EquipmentType.LAPTOP,
                "Dell", "XPS", "DUPLICATE-SN", null, null, null, siteId, null);

        when(equipmentRepository.existsBySerialNumber("DUPLICATE-SN")).thenReturn(true);

        assertThatThrownBy(() -> equipmentService.create(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("DUPLICATE-SN");
    }

    @Test
    void create_succeedsWithoutSerialNumber() {
        var dto = new EquipmentCreateDto("Monitor", EquipmentType.MONITOR,
                "LG", "27UK", null, null, null, null, siteId, null);

        when(equipmentRepository.nextSeq()).thenReturn(2L);
        Equipment savedEq = Equipment.builder().id(UUID.randomUUID())
                .name("Monitor").inventoryNumber("EQ-2026-00002").build();
        when(equipmentRepository.save(any())).thenReturn(savedEq);
        when(equipmentMapper.toDto(any())).thenReturn(null);

        equipmentService.create(dto);

        verify(equipmentRepository, never()).existsBySerialNumber(any());
    }

    // ─── assign ───────────────────────────────────────────────────────────────

    @Test
    void assign_throws_whenDifferentSite() {
        UUID eqId   = UUID.randomUUID();
        UUID eqSite = UUID.randomUUID();
        UUID userSite = UUID.randomUUID(); // different from eqSite

        Equipment eq = Equipment.builder().id(eqId).siteId(eqSite)
                .status(EquipmentStatus.IN_STOCK).build();
        User user = User.builder().id(userId).siteId(userSite).build();

        when(equipmentRepository.findById(eqId)).thenReturn(Optional.of(eq));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> equipmentService.assign(eqId, userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("іншого об'єкту");
    }

    @Test
    void assign_throws_whenDecommissioned() {
        UUID eqId = UUID.randomUUID();

        Equipment eq = Equipment.builder().id(eqId).siteId(siteId)
                .status(EquipmentStatus.DECOMMISSIONED).build();
        User user = User.builder().id(userId).siteId(siteId).build();

        when(equipmentRepository.findById(eqId)).thenReturn(Optional.of(eq));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> equipmentService.assign(eqId, userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("списане");
    }

    @Test
    void assign_succeeds_whenSameSite() {
        UUID eqId = UUID.randomUUID();

        Equipment eq = Equipment.builder().id(eqId).siteId(siteId)
                .status(EquipmentStatus.IN_STOCK).name("PC").build();
        User user = User.builder().id(userId).siteId(siteId)
                .username("worker").build();

        when(equipmentRepository.findById(eqId)).thenReturn(Optional.of(eq));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(equipmentRepository.save(any())).thenReturn(eq);
        when(equipmentMapper.toDto(any())).thenReturn(null);

        equipmentService.assign(eqId, userId);

        verify(equipmentRepository).save(eq);
        assertThat(eq.getStatus()).isEqualTo(EquipmentStatus.ASSIGNED);
        assertThat(eq.getAssignedUserId()).isEqualTo(userId);
        verify(notificationProducer).sendAssetAssigned(eq(userId), any(), any());
    }

    // ─── unassign ─────────────────────────────────────────────────────────────

    @Test
    void unassign_setsStatusInStock() {
        UUID eqId = UUID.randomUUID();
        Equipment eq = Equipment.builder().id(eqId).siteId(siteId)
                .status(EquipmentStatus.ASSIGNED).assignedUserId(userId).build();

        when(equipmentRepository.findById(eqId)).thenReturn(Optional.of(eq));
        when(equipmentRepository.save(any())).thenReturn(eq);
        when(equipmentMapper.toDto(any())).thenReturn(null);

        equipmentService.unassign(eqId);

        assertThat(eq.getStatus()).isEqualTo(EquipmentStatus.IN_STOCK);
        assertThat(eq.getAssignedUserId()).isNull();
    }

    // ─── generateInventoryNumber ───────────────────────────────────────────────

    @Test
    void inventoryNumberFormat_isCorrect() {
        var dto = new EquipmentCreateDto("Printer", EquipmentType.MONITOR,
                null, null, null, null, null, null, siteId, null);

        when(equipmentRepository.nextSeq()).thenReturn(42L);
        Equipment savedEq = Equipment.builder().id(UUID.randomUUID())
                .name("Printer").inventoryNumber("EQ-2026-00042").build();
        when(equipmentRepository.save(argThat(e ->
                e.getInventoryNumber() != null && e.getInventoryNumber().matches("EQ-\\d{4}-\\d{5}"))))
                .thenReturn(savedEq);
        when(equipmentMapper.toDto(any())).thenReturn(null);

        equipmentService.create(dto);

        verify(equipmentRepository).save(argThat(e ->
                e.getInventoryNumber().equals("EQ-2026-00042")));
    }
}
