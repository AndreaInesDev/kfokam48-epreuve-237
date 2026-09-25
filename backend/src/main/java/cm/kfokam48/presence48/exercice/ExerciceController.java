package cm.kfokam48.presence48.exercice;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code POST /api/exercices} — operation imposee, respectee a la lettre (B2).
 */
@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ExerciceService service;

    public ExerciceController(ExerciceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ExerciceDeposeDto> deposer(
            @Valid @RequestBody DeposerExerciceRequete requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.deposer(requete));
    }
}
