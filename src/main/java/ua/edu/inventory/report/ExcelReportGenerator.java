package ua.edu.inventory.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import ua.edu.inventory.equipment.Equipment;
import ua.edu.inventory.equipment.EquipmentRepository;
import ua.edu.inventory.license.License;
import ua.edu.inventory.license.LicenseRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Генератор Excel-звітів через Apache POI.
 * Створює форматовані .xlsx файли з обладнанням та ліцензіями.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelReportGenerator {

    private final EquipmentRepository equipmentRepository;
    private final LicenseRepository licenseRepository;

    // ─── Звіт по обладнанню ───────────────────────────────────────────────────

    public byte[] generateInventoryReport(UUID siteId) throws IOException {
        List<Equipment> items = (siteId != null)
                ? equipmentRepository.findAllBySiteId(siteId,
                        org.springframework.data.domain.Pageable.unpaged()).getContent()
                : equipmentRepository.findAll();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Інвентар обладнання");

            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle dateStyle   = createDateStyle(wb);

            // Заголовки
            String[] headers = {
                "Інв. номер", "Назва", "Тип", "Виробник", "Модель",
                "Серійний №", "Статус", "Дата купівлі", "Гарантія до",
                "Ціна (грн)", "Призначено (userId)"
            };
            createHeaderRow(sheet, headerStyle, headers);

            // Дані
            int rowNum = 1;
            for (Equipment eq : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(safe(eq.getInventoryNumber()));
                row.createCell(1).setCellValue(safe(eq.getName()));
                row.createCell(2).setCellValue(eq.getType().name());
                row.createCell(3).setCellValue(safe(eq.getManufacturer()));
                row.createCell(4).setCellValue(safe(eq.getModel()));
                row.createCell(5).setCellValue(safe(eq.getSerialNumber()));
                row.createCell(6).setCellValue(eq.getStatus().name());
                setDateCell(row, 7, eq.getPurchaseDate(), dateStyle);
                setDateCell(row, 8, eq.getWarrantyUntil(), dateStyle);
                if (eq.getPrice() != null) row.createCell(9).setCellValue(eq.getPrice().doubleValue());
                row.createCell(10).setCellValue(eq.getAssignedUserId() != null ? eq.getAssignedUserId().toString() : "");
            }
            autoSizeColumns(sheet, headers.length);
            return toBytes(wb);
        }
    }

    // ─── Звіт по ліцензіях ────────────────────────────────────────────────────

    public byte[] generateLicenseReport(UUID siteId) throws IOException {
        List<License> items = (siteId != null)
                ? licenseRepository.findAllBySiteId(siteId,
                        org.springframework.data.domain.Pageable.unpaged()).getContent()
                : licenseRepository.findAll();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Ліцензії");

            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle dateStyle   = createDateStyle(wb);

            String[] headers = {
                "ПЗ", "Вендор", "Тип", "Місць всього", "Використано",
                "Дата купівлі", "Дійсна до", "Вартість", "Призначено (userId)"
            };
            createHeaderRow(sheet, headerStyle, headers);

            int rowNum = 1;
            for (License lic : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(safe(lic.getSoftwareName()));
                row.createCell(1).setCellValue(safe(lic.getVendor()));
                row.createCell(2).setCellValue(lic.getLicenseType().name());
                row.createCell(3).setCellValue(lic.getSeatsTotal());
                row.createCell(4).setCellValue(lic.getSeatsUsed());
                setDateCell(row, 5, lic.getPurchaseDate(), dateStyle);
                setDateCell(row, 6, lic.getExpirationDate(), dateStyle);
                if (lic.getCost() != null) row.createCell(7).setCellValue(lic.getCost().doubleValue());
                row.createCell(8).setCellValue(lic.getAssignedUserId() != null ? lic.getAssignedUserId().toString() : "");
            }
            autoSizeColumns(sheet, headers.length);
            return toBytes(wb);
        }
    }

    // ─── Допоміжні методи ─────────────────────────────────────────────────────

    private void createHeaderRow(Sheet sheet, CellStyle style, String[] headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
        sheet.createFreezePane(0, 1);
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }

    private void setDateCell(Row row, int col, LocalDate date, CellStyle style) {
        if (date == null) return;
        Cell cell = row.createCell(col);
        cell.setCellValue(date.toString());
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) sheet.autoSizeColumn(i);
    }

    private String safe(String s) { return s != null ? s : ""; }

    private byte[] toBytes(XSSFWorkbook wb) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            wb.write(out);
            return out.toByteArray();
        }
    }
}
