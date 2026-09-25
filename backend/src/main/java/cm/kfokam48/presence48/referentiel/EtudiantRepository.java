package cm.kfokam48.presence48.referentiel;

import cm.kfokam48.presence48.domaine.Etudiant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acces aux etudiants. */
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    List<Etudiant> findByPromotionIdOrderByNomAsc(Long promotionId);
}
