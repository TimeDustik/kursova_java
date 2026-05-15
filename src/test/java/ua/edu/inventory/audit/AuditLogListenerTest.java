package ua.edu.inventory.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.edu.inventory.common.EntityType;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogListenerTest {

    @Mock AuditLogRepository auditLogRepository;

    @InjectMocks AuditLogListener auditLogListener;

    @Test
    void onEntityChanged_savesAuditLog() {
        UUID actorId  = UUID.randomUUID();
        UUID entityUUID = UUID.randomUUID();
        Map<String, Object> payload = Map.of("name", "TestDevice");

        var event = new EntityChangedEvent(
                this, AuditAction.CREATE, EntityType.EQUIPMENT,
                entityUUID.toString(), payload, actorId, "127.0.0.1");

        auditLogListener.onEntityChanged(event);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getAction()).isEqualTo(AuditAction.CREATE);
        assertThat(saved.getEntityType()).isEqualTo(EntityType.EQUIPMENT);
        assertThat(saved.getEntityId()).isEqualTo(entityUUID.toString());
        assertThat(saved.getActorUserId()).isEqualTo(actorId);
        assertThat(saved.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(saved.getPayload()).containsEntry("name", "TestDevice");
    }

    @Test
    void onEntityChanged_handlesNullActor() {
        var event = new EntityChangedEvent(
                this, AuditAction.DELETE, EntityType.USER,
                "some-id", Map.of(), null, null);

        auditLogListener.onEntityChanged(event);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getActorUserId()).isNull();
    }
}
