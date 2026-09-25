package cm.kfokam48.presence48.exercice;

import cm.kfokam48.presence48.domaine.Etudiant;
import cm.kfokam48.presence48.domaine.Exercice;
import cm.kfokam48.presence48.domaine.SessionCours;
import cm.kfokam48.presence48.domaine.StatutExercice;
import cm.kfokam48.presence48.erreur.CodeErreur;
import cm.kfokam48.presence48.erreur.ExceptionMetier;
import cm.kfokam48.presence48.presence.PresenceRepository;
import cm.kfokam48.presence48.relecture.AssignateurRelecteur;
import cm.kfokam48.presence48.referentiel.EtudiantRepository;
import cm.kfokam48.presence48.session.SessionRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Depot du lien d'un exercice (EF5, EF6).
 *
 * <p>Le point le plus facile a rater est RG11 : Q12 autorise explicitement un
 * depot tardif — « certains n'ont pas de connexion le soir meme ». L'expiration
 * du code ne doit donc PAS bloquer le depot. Seule la cloture le bloque (RG19).
 * C'est toute la distinction RG22 entre la fin d'une session et sa cloture.
 */
@Service
public class ExerciceService {

    /**
     * RG10 — le lien doit etre une URL http ou https. Verifiee ici et non par
     * annotation, parce que le contrat exige le code LIEN_INVALIDE pour ce cas
     * precis, la ou une contrainte declarative ressort en REQUETE_INVALIDE.
     */
    private static final Pattern URL_HTTP = Pattern.compile("^https?://\\S+$");

    private static final int LONGUEUR_MAX_LIEN = 500;

    private final ExerciceRepository exercices;
    private final SessionRepository sessions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final AssignateurRelecteur assignateur;
    private final Clock horloge;

    public ExerciceService(ExerciceRepository exercices, SessionRepository sessions,
            EtudiantRepository etudiants, PresenceRepository presences,
            AssignateurRelecteur assignateur, Clock horloge) {
        this.exercices = exercices;
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.assignateur = assignateur;
        this.horloge = horloge;
    }

    /**
     * EF5 — depose le lien d'un exercice pour une session.
     *
     * @throws ExceptionMetier {@code 404} session ou etudiant inconnu,
     *     {@code 409 SESSION_CLOTUREE} (RG19), {@code 409 ETUDIANT_NON_PRESENT}
     *     (RG23) ou {@code 409 EXERCICE_DEJA_DEPOSE} (RG9)
     */
    @Transactional
    public ExerciceDeposeDto deposer(DeposerExerciceRequete requete) {
        // RG10, avant toute autre verification : un lien absent, mal forme ou
        // trop long est refuse pour ce qu'il est, avec le code du contrat.
        if (requete.lien() == null
                || !URL_HTTP.matcher(requete.lien().trim()).matches()
                || requete.lien().trim().length() > LONGUEUR_MAX_LIEN) {
            throw new ExceptionMetier(CodeErreur.LIEN_INVALIDE);
        }

        SessionCours session = sessions.findById(requete.sessionId())
                .orElseThrow(() -> new ExceptionMetier(CodeErreur.SESSION_INCONNUE));
        Etudiant etudiant = etudiants.findById(requete.etudiantId())
                .orElseThrow(() -> new ExceptionMetier(CodeErreur.ETUDIANT_INCONNU));

        // RG19 : apres la cloture, plus aucun depot. RG11 : l'expiration du code,
        // elle, n'empeche rien — Q12 autorise le depot le soir meme.
        if (session.estCloturee()) {
            throw new ExceptionMetier(CodeErreur.SESSION_CLOTUREE);
        }

        // RG23 : on ne depose que pour une session ou sa presence est enregistree.
        if (!presences.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
            throw new ExceptionMetier(CodeErreur.ETUDIANT_NON_PRESENT);
        }

        // RG9 : un seul exercice par etudiant et par session.
        if (exercices.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
            throw new ExceptionMetier(CodeErreur.EXERCICE_DEJA_DEPOSE);
        }

        Exercice exercice = exercices.save(new Exercice(session, etudiant, requete.lien().trim(),
                StatutExercice.EN_ATTENTE_ASSIGNATION, OffsetDateTime.now(horloge)));

        // RG8 : le tirage au sort est un effet de bord du depot — c'est le seul
        // instant ou l'on connait a la fois l'exercice et la liste des presents.
        assignateur.assigner(exercice, presences.etudiantsPresentsA(session.getId()));

        return new ExerciceDeposeDto(exercice.getId(), exercice.getStatut());
    }
}
