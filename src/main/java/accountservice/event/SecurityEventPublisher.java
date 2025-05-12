package accountservice.event;

import accountservice.enums.SecurityEventType;
import accountservice.model.SecurityEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SecurityEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishEvent(SecurityEventType action, String subject, String object, String path) {
        SecurityEvent event = SecurityEvent.builder()
                .date(LocalDateTime.now())
                .action(action.name())
                .subject(subject)
                .object(object)
                .path(path)
                .build();

        eventPublisher.publishEvent(event);
    }
}
