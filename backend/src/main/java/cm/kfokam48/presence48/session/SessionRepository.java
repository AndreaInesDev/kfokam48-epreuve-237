package cm.kfokam48.presence48.session;

import cm.kfokam48.presence48.domaine.SessionCours;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acces aux sessions de cours. */
public interface SessionRepository extends JpaRepository<SessionCours, Long> {

    Optional<SessionCours> findByCode(String code);

    boolean existsByCode(String code);

    List<SessionCours> findByPromotionIdOrderByOuvertureAtDesc(Long promotionId);
}
