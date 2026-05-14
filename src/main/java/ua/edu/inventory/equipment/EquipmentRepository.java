package ua.edu.inventory.equipment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID>, JpaSpecificationExecutor<Equipment> {

    boolean existsByInventoryNumber(String inventoryNumber);

    boolean existsBySerialNumber(String serialNumber);

    Page<Equipment> findAllBySiteId(UUID siteId, Pageable pageable);

    List<Equipment> findAllByAssignedUserId(UUID assignedUserId);

    List<Equipment> findAllBySiteIdAndStatus(UUID siteId, EquipmentStatus status);

    long countBySiteId(UUID siteId);

    long countBySiteIdAndStatus(UUID siteId, EquipmentStatus status);

    /** Підраховує загальну кількість обладнання, що не знято з обліку */
    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.status <> ua.edu.inventory.equipment.EquipmentStatus.DECOMMISSIONED")
    long countActive();

    /** Наступне значення PostgreSQL-послідовності для формування інвентарного номера. */
    @Query(value = "SELECT nextval('equipment_seq')", nativeQuery = true)
    long nextSeq();
}
