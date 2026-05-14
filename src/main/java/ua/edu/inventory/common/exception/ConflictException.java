package ua.edu.inventory.common.exception;

/** Конфлікт унікальності або стану → HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
