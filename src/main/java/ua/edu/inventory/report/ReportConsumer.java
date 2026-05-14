package ua.edu.inventory.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Слухач черги звітів.
 * Виконується асинхронно у RabbitMQ-пулі потоків —
 * клієнт отримує 202 Accepted одразу, а звіт готується у фоні.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportConsumer {

    private final ExcelReportGenerator generator;
    private final ReportStore reportStore;

    @RabbitListener(queues = "${rabbitmq.queues.report}")
    public void consume(ReportMessage message) {
        log.info("Генерація звіту: type={} siteId={} reportId={}",
                message.reportType(), message.siteId(), message.reportId());

        reportStore.updateStatus(message.reportId(), ReportStatus.PROCESSING);
        try {
            byte[] content = switch (message.reportType()) {
                case "INVENTORY" -> generator.generateInventoryReport(message.siteId());
                case "LICENSES"  -> generator.generateLicenseReport(message.siteId());
                default -> throw new IllegalArgumentException("Невідомий тип звіту: " + message.reportType());
            };
            reportStore.saveContent(message.reportId(), content);
            log.info("Звіт готовий: reportId={} size={}b", message.reportId(), content.length);
        } catch (Exception e) {
            log.error("Помилка генерації звіту {}: {}", message.reportId(), e.getMessage(), e);
            reportStore.updateStatus(message.reportId(), ReportStatus.FAILED);
        }
    }
}
