package accountservice.controller;

import accountservice.repository.SecurityEventRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class SecurityEventController {

    SecurityEventRepository securityEventRepository;

    @GetMapping("/api/security/events/")
    public ResponseEntity<?> getAllSecurityEvents() {
        return ResponseEntity.ok(securityEventRepository.findAll());
    }
}
