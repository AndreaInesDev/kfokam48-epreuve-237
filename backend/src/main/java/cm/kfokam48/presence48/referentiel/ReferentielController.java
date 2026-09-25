package cm.kfokam48.presence48.referentiel;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Referentiel en lecture seule — operations ajoutees au contrat (EF2).
 *
 * <p>Le controleur ne fait que traduire HTTP en appel de service : aucune
 * requete base, aucune regle metier ici (B3).
 */
@RestController
@RequestMapping("/api/promotions")
public class ReferentielController {

    private final ReferentielService service;

    public ReferentielController(ReferentielService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PromotionDto>> listerPromotions() {
        return ResponseEntity.ok(service.listerPromotions());
    }

    @GetMapping("/{id}/etudiants")
    public ResponseEntity<List<EtudiantDto>> listerEtudiants(@PathVariable Long id) {
        return ResponseEntity.ok(service.listerEtudiants(id));
    }
}
