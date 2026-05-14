package ua.edu.inventory.report;

import lombok.Getter;
import ua.edu.inventory.equipment.EquipmentStatus;
import ua.edu.inventory.equipment.EquipmentType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pattern: Builder.
 * Reason: запит на звіт містить багато опціональних параметрів;
 * Builder дозволяє будувати об'єкт покроково, без телескопічних конструкторів,
 * і гарантує незмінність після виклику build().
 *
 * Використання:
 *   EquipmentReportRequest req = EquipmentReportRequest.builder()
 *       .siteId(siteId)
 *       .from(LocalDate.now().minusMonths(3))
 *       .statuses(List.of(ASSIGNED, IN_STOCK))
 *       .build();
 */
@Getter
public final class EquipmentReportRequest {

    private final UUID siteId;
    private final LocalDate from;
    private final LocalDate to;
    private final List<EquipmentStatus> statuses;
    private final List<EquipmentType> types;

    private EquipmentReportRequest(Builder b) {
        this.siteId   = b.siteId;
        this.from     = b.from;
        this.to       = b.to;
        this.statuses = List.copyOf(b.statuses);
        this.types    = List.copyOf(b.types);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private UUID siteId;
        private LocalDate from;
        private LocalDate to;
        private final List<EquipmentStatus> statuses = new ArrayList<>();
        private final List<EquipmentType>   types    = new ArrayList<>();

        public Builder siteId(UUID siteId)               { this.siteId = siteId; return this; }
        public Builder from(LocalDate from)               { this.from = from;     return this; }
        public Builder to(LocalDate to)                   { this.to = to;         return this; }
        public Builder statuses(List<EquipmentStatus> s)  { statuses.addAll(s);   return this; }
        public Builder types(List<EquipmentType> t)       { types.addAll(t);      return this; }

        public EquipmentReportRequest build() { return new EquipmentReportRequest(this); }
    }
}
