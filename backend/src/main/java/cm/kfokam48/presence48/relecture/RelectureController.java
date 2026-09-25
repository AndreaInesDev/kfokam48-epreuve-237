package cm.kfokam48.presence48.relecture;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code POST /api/relectures/{id}} — operation imposee, respectee a la lettre :
 * le succes est un {@code 200} sans corps, comme le contrat le declare (B2).
 */
@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

    private final RelectureService service;

    public RelectureController(RelectureService service) {
        this.service = service;
    }

    /** EF8 — operation ajoutee : ce que ce relecteur doit encore rendre. */
    @GetMapping
    public ResponseEntity<List<RelectureATraiterDto>> aRendre(@RequestParam Long relecteurId) {
        return ResponseEntity.ok(service.aRendrePar(relecteurId));
    }

    /** RG12 — operation ajoutee : marque le debut de la relecture. */
    @PostMapping("/{id}/ouverture")
    public ResponseEntity<RelectureATraiterDto> ouvrir(@PathVariable Long id,
            @RequestParam(required = false) Long relecteurId) {
        return ResponseEntity.ok(service.ouvrir(id, relecteurId));
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> rendre(@PathVariable Long id,
            @RequestBody RendreRelectureRequete requete) {
        service.rendre(id, requete);
        return ResponseEntity.ok().build();
    }
}
