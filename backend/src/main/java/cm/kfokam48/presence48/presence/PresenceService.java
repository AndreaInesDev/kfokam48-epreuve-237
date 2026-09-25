package cm.kfokam48.presence48.presence;

import cm.kfokam48.presence48.domaine.Etudiant;
import cm.kfokam48.presence48.domaine.Presence;
import cm.kfokam48.presence48.domaine.SessionCours;
import cm.kfokam48.presence48.domaine.SourcePresence;
import cm.kfokam48.presence48.erreur.CodeErreur;
import cm.kfokam48.presence48.erreur.ExceptionMetier;
import cm.kfokam48.presence48.exercice.ExerciceRepository;
import cm.kfokam48.presence48.referentiel.EtudiantRepository;
import cm.kfokam48.presence48.relecture.AssignateurRelecteur;
import cm.kfokam48.presence48.session.SessionRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Marquage de presence (EF3, EF4).
 *
 * <p>L'ordre des verifications est celui de {@code docs/diagrammes/D3} et il
 * n'est pas neutre :
 *
 * <ol>
 *   <li><b>code inconnu</b> avant <b>expiration</b> : on ne peut pas dire qu'un
 *       code est expire tant qu'on ne l'a pas trouve. C'est aussi pourquoi le
 *       contrat repond 400 pour un code inconnu et 410 pour un code expire —
 *       {@code 410 Gone} signifie « a existe, n'existe plus », ce qui n'a de
 *       sens que pour un code reellement emis ;</li>
 *   <li><b>expiration</b> avant <b>doublon</b> : repondre 409 sur un code
 *       expire renseignerait l'etudiant sur la validite passee du code sans
 *       qu'il ait eu besoin d'un code valide.</li>
 * </ol>
 *
 * <p>Le blocage de RG17, qui se place avant la recherche du code, arrive avec
 * l'issue #15 — priorisee Could.
 */
@Service
public class PresenceService {

    private final SessionRepository sessions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final AssignateurRelecteur assignateur;
    private final Clock horloge;

    public PresenceService(SessionRepository sessions, EtudiantRepository etudiants,
            PresenceRepository presences, ExerciceRepository exercices,
            AssignateurRelecteur assignateur, Clock horloge) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.exercices = exercices;
        this.assignateur = assignateur;
        this.horloge = horloge;
    }

    /**
     * EF3 — enregistre la presence d'un etudiant a partir du code de session.
     *
     * @throws ExceptionMetier {@code 400 CODE_INCONNU}, {@code 410 CODE_EXPIRE}
     *     (RG1, RG5), {@code 409 DEJA_PRESENT} (RG4) ou
     *     {@code 404 ETUDIANT_INCONNU}
     */
    @Transactional
    public PresenceDto marquer(MarquerPresenceRequete requete) {
        SessionCours session = sessions.findByCode(requete.code())
                .orElseThrow(() -> new ExceptionMetier(CodeErreur.CODE_INCONNU));

        // RG1 et RG5 : passe l'expiration, le code ne marche plus, et Q3 interdit
        // de se declarer present apres la fin de la session.
        if (session.codeExpireA(OffsetDateTime.now(horloge))) {
            throw new ExceptionMetier(CodeErreur.CODE_EXPIRE);
        }

        Etudiant etudiant = etudiants.findById(requete.etudiantId())
                .orElseThrow(() -> new ExceptionMetier(CodeErreur.ETUDIANT_INCONNU));

        // RG4 : une seule presence par etudiant et par session.
        if (presences.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
            throw new ExceptionMetier(CodeErreur.DEJA_PRESENT);
        }

        // RG15 : une presence saisie par l'etudiant porte la source ETUDIANT.
        Presence presence = presences.save(new Presence(
                session, etudiant, SourcePresence.ETUDIANT, OffsetDateTime.now(horloge)));

        // RG20 : l'arrivee d'un present rend le tirage possible pour les exercices
        // restes sans relecteur. D3 montre cet effet de bord du cas nominal.
        var presentsMaintenant = presences.etudiantsPresentsA(session.getId());
        exercices.findBySessionIdAndStatut(
                        session.getId(), cm.kfokam48.presence48.domaine.StatutExercice.EN_ATTENTE_ASSIGNATION)
                .forEach(orphelin -> assignateur.assigner(orphelin, presentsMaintenant));

        return new PresenceDto(presence.getId(), session.getId(), etudiant.getId(),
                presence.getSource());
    }
}
