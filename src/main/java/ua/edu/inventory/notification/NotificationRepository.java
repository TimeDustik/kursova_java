package ua.edu.inventory.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findAllByRecipientUserId(UUID recipientUserId, Pageable pageable);

    List<Notification> findAllByStatus(NotificationStatus status);

    long countByRecipientUserIdAndStatus(UUID recipientUserId, NotificationStatus status);
}
