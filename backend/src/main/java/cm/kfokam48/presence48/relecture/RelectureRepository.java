package cm.kfokam48.presence48.relecture;

import cm.kfokam48.presence48.domaine.Relecture;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acces aux relectures. */
public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    Optional<Relecture> findByExerciceId(Long exerciceId);

    /** EF8 — ce qu'un relecteur doit encore rendre (RG16). */
    List<Relecture> findByRelecteurIdAndRendueAtIsNullOrderByAssigneeAtAsc(Long relecteurId);
}
