package ua.edu.inventory.license;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<License, UUID>, JpaSpecificationExecutor<License> {

    Page<License> findAllBySiteId(UUID siteId, Pageable pageable);

    List<License> findAllByAssignedUserId(UUID assignedUserId);

    long countBySiteId(UUID siteId);

    /** Ліцензії що спливають протягом наступних днів (для RabbitMQ-планувальника). */
    @Query("SELECT l FROM License l WHERE l.expirationDate IS NOT NULL " +
           "AND l.expirationDate <= :threshold AND l.expirationDate >= CURRENT_DATE")
    List<License> findExpiringBefore(@Param("threshold") LocalDate threshold);

    /** Для dashboard: підрахунок ліцензій із вичерпаними місцями. */
    @Query("SELECT COUNT(l) FROM License l WHERE l.siteId = :siteId AND l.seatsUsed >= l.seatsTotal")
    long countExhaustedBySiteId(@Param("siteId") UUID siteId);
}
