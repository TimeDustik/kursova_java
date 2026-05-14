package ua.edu.inventory.notification;

/**
 * Pattern: Strategy.
 * Reason: інкапсулює різні канали доставки сповіщень (Email, InApp, Telegram)
 * за єдиним інтерфейсом. NotificationService обирає реалізацію динамічно
 * за допомогою ін'єктованого списку стратегій — новий канал додається
 * без зміни існуючого коду (принцип Open/Closed — SOLID).
 */
public interface NotificationStrategy {

    /**
     * Відправити сповіщення через канал цієї стратегії.
     *
     * @param notification сутність сповіщення
     */
    void send(Notification notification);

    /**
     * Перевірити, чи підтримує ця стратегія даний тип сповіщення.
     *
     * @param type тип сповіщення
     * @return true якщо стратегія обробляє цей тип
     */
    boolean supports(NotificationType type);
}
