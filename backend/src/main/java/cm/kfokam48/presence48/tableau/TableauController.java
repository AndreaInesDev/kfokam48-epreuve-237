package cm.kfokam48.presence48.tableau;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code GET /api/tableau?promotionId=} — operation imposee, respectee a la
 * lettre (B2).
 *
 * <p>La contrainte F3 interdit au frontend de recalculer la moyenne : la valeur
 * affichee est celle que renvoie cette operation, et nulle autre.
 */
@RestController
@RequestMapping("/api/tableau")
public class TableauController {

    private final TableauService service;

    public TableauController(TableauService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<LigneTableauDto>> tableau(@RequestParam Long promotionId) {
        return ResponseEntity.ok(service.pourPromotion(promotionId));
    }
}
