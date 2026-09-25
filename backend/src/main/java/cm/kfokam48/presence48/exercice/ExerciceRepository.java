package cm.kfokam48.presence48.exercice;

import cm.kfokam48.presence48.domaine.Exercice;
import cm.kfokam48.presence48.domaine.StatutExercice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acces aux exercices. */
public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    /** RG9 — un seul exercice par etudiant et par session. */
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    /** RG20 — les exercices d'une session restes sans relecteur. */
    List<Exercice> findBySessionIdAndStatut(Long sessionId, StatutExercice statut);

    List<Exercice> findByEtudiantIdOrderByDeposeAtDesc(Long etudiantId);
}
