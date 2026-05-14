package ua.edu.inventory.equipment;

import ua.edu.inventory.equipment.dto.EquipmentCreateDto;

/**
 * Pattern: Factory Method.
 * Reason: делегує створення Equipment конкретним підкласам,
 * що дозволяє встановлювати дефолти залежно від типу обладнання
 * (наприклад, гарантія 3 роки для ноутбуків, 5 років для мережевого обладнання).
 *
 * Використання:
 *   EquipmentFactory factory = EquipmentFactory.forType(dto.type());
 *   Equipment eq = factory.createWithDefaults(dto, inventoryNumber);
 */
public abstract class EquipmentFactory {

    /**
     * Factory method — підкласи визначають конкретну логіку ініціалізації.
     */
    public abstract Equipment createWithDefaults(EquipmentCreateDto dto, String inventoryNumber);

    /** Повертає відповідну фабрику для заданого типу обладнання. */
    public static EquipmentFactory forType(EquipmentType type) {
        return switch (type) {
            case LAPTOP, DESKTOP -> new ComputerEquipmentFactory();
            case NETWORK         -> new NetworkEquipmentFactory();
            default              -> new DefaultEquipmentFactory();
        };
    }

    // ─── Конкретні фабрики ─────────────────────────────────────────────────────

    /**
     * Фабрика для комп'ютерного обладнання.
     * Дефолт: гарантія 3 роки від дати купівлі.
     */
    static class ComputerEquipmentFactory extends EquipmentFactory {
        @Override
        public Equipment createWithDefaults(EquipmentCreateDto dto, String inventoryNumber) {
            Equipment eq = baseEquipment(dto, inventoryNumber);
            if (eq.getWarrantyUntil() == null && eq.getPurchaseDate() != null) {
                eq.setWarrantyUntil(eq.getPurchaseDate().plusYears(3));
            }
            return eq;
        }
    }

    /**
     * Фабрика для мережевого обладнання.
     * Дефолт: гарантія 5 років від дати купівлі.
     */
    static class NetworkEquipmentFactory extends EquipmentFactory {
        @Override
        public Equipment createWithDefaults(EquipmentCreateDto dto, String inventoryNumber) {
            Equipment eq = baseEquipment(dto, inventoryNumber);
            if (eq.getWarrantyUntil() == null && eq.getPurchaseDate() != null) {
                eq.setWarrantyUntil(eq.getPurchaseDate().plusYears(5));
            }
            return eq;
        }
    }

    /**
     * Фабрика за замовчуванням (монітори, принтери, телефони тощо).
     * Дефолт: гарантія 1 рік від дати купівлі.
     */
    static class DefaultEquipmentFactory extends EquipmentFactory {
        @Override
        public Equipment createWithDefaults(EquipmentCreateDto dto, String inventoryNumber) {
            Equipment eq = baseEquipment(dto, inventoryNumber);
            if (eq.getWarrantyUntil() == null && eq.getPurchaseDate() != null) {
                eq.setWarrantyUntil(eq.getPurchaseDate().plusYears(1));
            }
            return eq;
        }
    }

    protected static Equipment baseEquipment(EquipmentCreateDto dto, String inventoryNumber) {
        return Equipment.builder()
                .inventoryNumber(inventoryNumber)
                .name(dto.name())
                .type(dto.type())
                .manufacturer(dto.manufacturer())
                .model(dto.model())
                .serialNumber(dto.serialNumber())
                .status(EquipmentStatus.IN_STOCK)
                .purchaseDate(dto.purchaseDate())
                .warrantyUntil(dto.warrantyUntil())
                .price(dto.price())
                .siteId(dto.siteId())
                .notes(dto.notes())
                .build();
    }
}
