package ua.edu.inventory.common.exception;

import java.util.UUID;

/** Ресурс не знайдено → HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entityName, UUID id) {
        super(entityName + " з id=" + id + " не знайдено");
    }

    public ResourceNotFoundException(String entityName, String field, Object value) {
        super(entityName + " з " + field + "=" + value + " не знайдено");
    }
}
