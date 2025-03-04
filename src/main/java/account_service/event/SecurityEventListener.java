package account_service.event;

import account_service.model.SecurityEvent;
import account_service.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityEventListener {

    private final SecurityEventRepository securityEventRepository;

    @EventListener
    public void handleSecurityEvent(SecurityEvent event){
        securityEventRepository.save(event);
    }
}
