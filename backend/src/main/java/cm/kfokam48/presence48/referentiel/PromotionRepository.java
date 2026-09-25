package cm.kfokam48.presence48.referentiel;

import cm.kfokam48.presence48.domaine.Promotion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acces aux promotions. Aucune requete ne remonte jamais dans un controleur (B3). */
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findAllByOrderByNomAsc();
}
