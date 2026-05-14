package ua.edu.inventory.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.edu.inventory.equipment.Equipment;
import ua.edu.inventory.equipment.EquipmentRepository;
import ua.edu.inventory.license.License;
import ua.edu.inventory.license.LicenseRepository;
import ua.edu.inventory.site.Site;
import ua.edu.inventory.site.SiteRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Планувальник сповіщень про ліцензії та гарантії, що спливають.
 * Виконується щодня о 08:00 UTC.
 *
 * Ліцензії: якщо expirationDate < now + 30 днів → сповіщення тимліду сайту.
 * Обладнання: якщо warrantyUntil < now + 30 днів → сповіщення тимліду сайту.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LicenseExpiryScheduler {

    private static final int DAYS_AHEAD = 30;

    private final LicenseRepository licenseRepository;
    private final EquipmentRepository equipmentRepository;
    private final SiteRepository siteRepository;
    private final NotificationProducer notificationProducer;

    @Scheduled(cron = "0 0 8 * * *")
    public void checkExpiringLicenses() {
        LocalDate threshold = LocalDate.now().plusDays(DAYS_AHEAD);
        List<License> expiring = licenseRepository.findExpiringBefore(threshold);

        if (expiring.isEmpty()) {
            log.debug("Немає ліцензій що спливають протягом {} днів", DAYS_AHEAD);
            return;
        }

        log.info("Знайдено {} ліцензій що спливають до {}", expiring.size(), threshold);

        expiring.forEach(license -> {
            UUID recipientId = resolveTeamLeadId(license.getSiteId());
            if (recipientId == null) return;

            notificationProducer.sendLicenseExpiry(
                    recipientId,
                    license.getSoftwareName(),
                    license.getExpirationDate().toString(),
                    license.getId()
            );
        });
    }

    @Scheduled(cron = "0 30 8 * * *")
    public void checkExpiringWarranties() {
        LocalDate threshold = LocalDate.now().plusDays(DAYS_AHEAD);

        List<Equipment> expiring = equipmentRepository.findAll().stream()
                .filter(e -> e.getWarrantyUntil() != null
                          && !e.getWarrantyUntil().isAfter(threshold)
                          && !e.getWarrantyUntil().isBefore(LocalDate.now()))
                .toList();

        if (expiring.isEmpty()) return;

        log.info("Знайдено {} одиниць обладнання з гарантією що спливає до {}", expiring.size(), threshold);

        expiring.forEach(eq -> {
            UUID recipientId = resolveTeamLeadId(eq.getSiteId());
            if (recipientId == null) return;

            notificationProducer.sendWarrantyExpiry(
                    recipientId,
                    eq.getName(),
                    eq.getWarrantyUntil().toString(),
                    eq.getId()
            );
        });
    }

    private UUID resolveTeamLeadId(UUID siteId) {
        if (siteId == null) return null;
        return siteRepository.findById(siteId)
                .map(Site::getTeamLeadId)
                .orElse(null);
    }
}
