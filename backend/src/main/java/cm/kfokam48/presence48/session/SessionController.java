package cm.kfokam48.presence48.session;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code POST /api/sessions} est l'une des cinq operations imposees : chemin,
 * verbe, corps et codes de statut sont respectes a la lettre (B2).
 * {@code GET /api/sessions} est une operation ajoutee, pour les ecrans formateur.
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SessionOuverteDto> ouvrir(@Valid @RequestBody OuvrirSessionRequete requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ouvrir(requete));
    }

    @GetMapping
    public ResponseEntity<List<SessionDto>> lister(@RequestParam Long promotionId) {
        return ResponseEntity.ok(service.listerParPromotion(promotionId));
    }
}
