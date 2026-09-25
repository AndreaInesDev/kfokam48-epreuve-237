package cm.kfokam48.presence48.presence;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code POST /api/presences} — operation imposee, respectee a la lettre (B2).
 */
@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService service;

    public PresenceController(PresenceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PresenceDto> marquer(@Valid @RequestBody MarquerPresenceRequete requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.marquer(requete));
    }
}
