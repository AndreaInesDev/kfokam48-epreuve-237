package cm.kfokam48.presence48.presence;

import cm.kfokam48.presence48.domaine.Presence;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acces aux presences. */
public interface PresenceRepository extends JpaRepository<Presence, Long> {

    /** RG4 — un etudiant n'est present qu'une fois a une meme session. */
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
}
