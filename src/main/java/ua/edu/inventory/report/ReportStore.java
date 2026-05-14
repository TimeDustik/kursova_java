package ua.edu.inventory.report;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory сховище згенерованих звітів.
 * Pattern: Singleton — один екземпляр у Spring-контексті.
 *
 * У production-системі замість ConcurrentHashMap використовувалось би
 * постійне сховище (S3, БД) для стійкості між перезапусками.
 */
@Component
public class ReportStore {

    private final Map<UUID, Entry> store = new ConcurrentHashMap<>();

    public void save(UUID reportId, ReportStatus status, byte[] content, String filename) {
        store.put(reportId, new Entry(status, content, filename));
    }

    public void updateStatus(UUID reportId, ReportStatus status) {
        store.computeIfPresent(reportId, (k, e) -> { e.setStatus(status); return e; });
    }

    public void saveContent(UUID reportId, byte[] content) {
        store.computeIfPresent(reportId, (k, e) -> { e.setContent(content); e.setStatus(ReportStatus.READY); return e; });
    }

    public Optional<Entry> find(UUID reportId) {
        return Optional.ofNullable(store.get(reportId));
    }

    @Getter @Setter
    public static class Entry {
        private ReportStatus status;
        private byte[] content;
        private final String filename;

        public Entry(ReportStatus status, byte[] content, String filename) {
            this.status   = status;
            this.content  = content;
            this.filename = filename;
        }

        public boolean isReady() { return status == ReportStatus.READY; }
    }
}
