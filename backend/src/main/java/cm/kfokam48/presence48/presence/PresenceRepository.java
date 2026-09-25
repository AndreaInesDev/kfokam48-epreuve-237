package cm.kfokam48.presence48.presence;

import cm.kfokam48.presence48.domaine.Etudiant;
import cm.kfokam48.presence48.domaine.Presence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Acces aux presences. */
public interface PresenceRepository extends JpaRepository<Presence, Long> {

    /** RG4 — un etudiant n'est present qu'une fois a une meme session. */
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    /**
     * RG8 — l'ensemble dans lequel se fait le tirage au sort : les etudiants
     * presents a cette session, quelle que soit la source de leur presence.
     * Une presence ajoutee par le formateur (RG15) rend donc eligible, ce qui
     * est voulu : Q14 corrige un incident materiel, pas une absence.
     */
    @Query("select p.etudiant from Presence p where p.session.id = :sessionId")
    List<Etudiant> etudiantsPresentsA(Long sessionId);
}
