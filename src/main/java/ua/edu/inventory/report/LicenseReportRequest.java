package ua.edu.inventory.report;

import lombok.Getter;
import ua.edu.inventory.license.LicenseType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pattern: Builder — аналог EquipmentReportRequest для звіту по ліцензіях.
 */
@Getter
public final class LicenseReportRequest {

    private final UUID siteId;
    private final LocalDate expiringBefore;
    private final List<LicenseType> licenseTypes;

    private LicenseReportRequest(Builder b) {
        this.siteId         = b.siteId;
        this.expiringBefore = b.expiringBefore;
        this.licenseTypes   = List.copyOf(b.licenseTypes);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private UUID siteId;
        private LocalDate expiringBefore;
        private final List<LicenseType> licenseTypes = new ArrayList<>();

        public Builder siteId(UUID siteId)                  { this.siteId = siteId;                 return this; }
        public Builder expiringBefore(LocalDate d)          { this.expiringBefore = d;              return this; }
        public Builder licenseTypes(List<LicenseType> lt)   { licenseTypes.addAll(lt);              return this; }

        public LicenseReportRequest build() { return new LicenseReportRequest(this); }
    }
}
