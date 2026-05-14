package ua.edu.inventory.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ua.edu.inventory.auth.UserPrincipal;
import ua.edu.inventory.common.SecurityUtils;

import java.util.UUID;

/** Відправляє запити на генерацію звітів до RabbitMQ. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ReportStore reportStore;

    @Value("${rabbitmq.exchanges.inventory}")
    private String exchange;

    @Value("${rabbitmq.routing-keys.report}")
    private String routingKey;

    public UUID requestInventoryReport(UUID siteId) {
        return enqueue("INVENTORY", siteId,
                "inventory-report-" + UUID.randomUUID() + ".xlsx");
    }

    public UUID requestLicenseReport(UUID siteId) {
        return enqueue("LICENSES", siteId,
                "license-report-" + UUID.randomUUID() + ".xlsx");
    }

    private UUID enqueue(String reportType, UUID siteId, String filename) {
        UUID reportId = UUID.randomUUID();
        UUID actorId  = SecurityUtils.getCurrentPrincipal().map(UserPrincipal::getId).orElse(null);

        // Реєструємо у сховищі зі статусом PENDING
        reportStore.save(reportId, ReportStatus.PENDING, null, filename);

        ReportMessage msg = new ReportMessage(reportId, reportType, actorId, siteId, filename);
        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
        log.info("Запит на звіт {} відправлено: reportId={}", reportType, reportId);
        return reportId;
    }
}
