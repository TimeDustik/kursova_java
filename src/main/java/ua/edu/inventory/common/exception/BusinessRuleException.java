package ua.edu.inventory.common.exception;

/** Порушення бізнес-правила → HTTP 422. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
