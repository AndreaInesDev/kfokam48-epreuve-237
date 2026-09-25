package cm.kfokam48.presence48.relecture;

import cm.kfokam48.presence48.domaine.Relecture;
import cm.kfokam48.presence48.referentiel.EtudiantRepository;
import cm.kfokam48.presence48.domaine.StatutExercice;
import cm.kfokam48.presence48.erreur.CodeErreur;
import cm.kfokam48.presence48.erreur.ExceptionMetier;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rendu d'une relecture (EF9, EF10).
 *
 * <p>RG13 — une relecture validee est definitive. C'est la contradiction
 * Q10 / Q15 tranchee en faveur de Q15, et c'est le
 * {@code 409 RELECTURE_DEJA_RENDUE} qui la materialise : le contrat impose ce
 * code, donc choisir Q10 aurait viole la contrainte B2.
 */
@Service
public class RelectureService {

    private static final int NOTE_MIN = 0;
    private static final int NOTE_MAX = 20;
    private static final int LONGUEUR_MAX_COMMENTAIRE = 2000;

    private final RelectureRepository relectures;
    private final EtudiantRepository etudiants;
    private final Clock horloge;

    public RelectureService(RelectureRepository relectures, EtudiantRepository etudiants,
            Clock horloge) {
        this.relectures = relectures;
        this.etudiants = etudiants;
        this.horloge = horloge;
    }

    /**
     * EF8 — ce que ce relecteur doit encore rendre (RG16).
     *
     * <p>Sans cette operation, la contrainte F2 serait infaisable : rien ne
     * dirait au relecteur quel exercice relire, puisque c'est le systeme qui le
     * lui assigne (RG8) et qu'il ne le choisit pas.
     *
     * @throws ExceptionMetier {@code 404 ETUDIANT_INCONNU}
     */
    @Transactional(readOnly = true)
    public List<RelectureATraiterDto> aRendrePar(Long relecteurId) {
        if (!etudiants.existsById(relecteurId)) {
            throw new ExceptionMetier(CodeErreur.ETUDIANT_INCONNU);
        }
        return relectures.findByRelecteurIdAndRendueAtIsNullOrderByAssigneeAtAsc(relecteurId)
                .stream()
                .map(RelectureService::versDto)
                .toList();
    }

    /**
     * RG12 — le relecteur ouvre l'exercice : la relecture commence, et l'auteur
     * ne peut plus remplacer son lien. Idempotent : un second appel ne deplace
     * pas l'instant de premiere ouverture.
     *
     * @throws ExceptionMetier {@code 404 RELECTURE_INCONNUE},
     *     {@code 403 RELECTURE_NON_ASSIGNEE} ou {@code 409 RELECTURE_DEJA_RENDUE}
     */
    @Transactional
    public RelectureATraiterDto ouvrir(Long relectureId, Long relecteurId) {
        Relecture relecture = relectures.findById(relectureId)
                .orElseThrow(() -> new ExceptionMetier(CodeErreur.RELECTURE_INCONNUE));

        if (relecteurId != null && !relecteurId.equals(relecture.getRelecteur().getId())) {
            throw new ExceptionMetier(CodeErreur.RELECTURE_NON_ASSIGNEE);
        }
        if (relecture.estRendue()) {
            throw new ExceptionMetier(CodeErreur.RELECTURE_DEJA_RENDUE);
        }

        relecture.ouvrir(OffsetDateTime.now(horloge));
        // D4 : l'exercice passe de EN_ATTENTE_RELECTURE a EN_COURS_DE_RELECTURE.
        relecture.getExercice().changerStatut(StatutExercice.EN_COURS_DE_RELECTURE);
        return versDto(relecture);
    }

    private static RelectureATraiterDto versDto(Relecture relecture) {
        return new RelectureATraiterDto(
                relecture.getId(),
                relecture.getExercice().getId(),
                relecture.getExercice().getLien(),
                relecture.getExercice().getSession().getTitre(),
                relecture.estCommencee());
    }

    /**
     * EF9 — enregistre la note et le commentaire, definitivement.
     *
     * @throws ExceptionMetier {@code 404 RELECTURE_INCONNUE},
     *     {@code 400 NOTE_INVALIDE} (RG3), {@code 403 AUTO_RELECTURE} (RG2),
     *     {@code 403 RELECTURE_NON_ASSIGNEE}, {@code 409 SESSION_CLOTUREE}
     *     (RG19) ou {@code 409 RELECTURE_DEJA_RENDUE} (RG13)
     */
    @Transactional
    public void rendre(Long relectureId, RendreRelectureRequete requete) {
        Relecture relecture = relectures.findById(relectureId)
                .orElseThrow(() -> new ExceptionMetier(CodeErreur.RELECTURE_INCONNUE));

        // RG3 : entier de 0 a 20. Verifie ici pour porter le code du contrat.
        if (requete.note() == null
                || requete.note() < NOTE_MIN
                || requete.note() > NOTE_MAX) {
            throw new ExceptionMetier(CodeErreur.NOTE_INVALIDE);
        }
        if (requete.commentaire() == null || requete.commentaire().isBlank()) {
            throw new ExceptionMetier(CodeErreur.REQUETE_INVALIDE,
                    "Le commentaire de relecture est obligatoire.");
        }
        if (requete.commentaire().length() > LONGUEUR_MAX_COMMENTAIRE) {
            throw new ExceptionMetier(CodeErreur.REQUETE_INVALIDE,
                    "Le commentaire ne peut pas depasser 2000 caracteres.");
        }

        // RG2, en garde-fou. Le tirage au sort exclut deja l'auteur, donc ce cas
        // ne devrait jamais se produire ; s'il se produit, c'est un defaut grave
        // et il vaut mieux un 403 qu'une note enregistree en violation de Q5.
        if (relecture.getRelecteur().getId()
                .equals(relecture.getExercice().getEtudiant().getId())) {
            throw new ExceptionMetier(CodeErreur.AUTO_RELECTURE);
        }

        // Ajout facultatif : si l'appelant se declare, on verifie que c'est bien
        // lui. Sans authentification (Q1), l'API ne peut pas faire mieux.
        if (requete.relecteurId() != null
                && !requete.relecteurId().equals(relecture.getRelecteur().getId())) {
            throw new ExceptionMetier(CodeErreur.RELECTURE_NON_ASSIGNEE);
        }

        // RG13 : definitive. Verifie apres l'identite, pour ne pas reveler a un
        // tiers qu'une relecture a deja ete rendue.
        if (relecture.estRendue()) {
            throw new ExceptionMetier(CodeErreur.RELECTURE_DEJA_RENDUE);
        }

        // RG19 : plus aucun rendu apres la cloture de la session.
        if (relecture.getExercice().getSession().estCloturee()) {
            throw new ExceptionMetier(CodeErreur.SESSION_CLOTUREE);
        }

        relecture.rendre(requete.note(), requete.commentaire().trim(),
                OffsetDateTime.now(horloge));
        relecture.getExercice().changerStatut(StatutExercice.RELU);
    }
}
