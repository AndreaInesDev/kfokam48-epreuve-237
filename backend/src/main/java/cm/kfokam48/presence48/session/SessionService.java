package cm.kfokam48.presence48.session;

import cm.kfokam48.presence48.config.ParametresPresence48;
import cm.kfokam48.presence48.domaine.Promotion;
import cm.kfokam48.presence48.domaine.SessionCours;
import cm.kfokam48.presence48.erreur.CodeErreur;
import cm.kfokam48.presence48.erreur.ExceptionMetier;
import cm.kfokam48.presence48.referentiel.PromotionRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ouverture et consultation des sessions de cours (EF1).
 *
 * <p>L'horloge est injectee plutot que lue par {@code OffsetDateTime.now()} :
 * RG1 est une regle d'ecart temporel, et un test doit pouvoir la verifier sans
 * attendre quinze minutes.
 */
@Service
public class SessionService {

    /**
     * Nombre de tirages tentes avant d'abandonner sur collision de code. Avec
     * un alphabet de 31 caracteres sur 8 positions, la collision est
     * improbable ; mais RG6 est garantie par une contrainte UNIQUE en base, et
     * on prefere reessayer proprement plutot que remonter une violation SQL.
     */
    private static final int TIRAGES_MAX = 10;

    private final SessionRepository sessions;
    private final PromotionRepository promotions;
    private final GenerateurCode generateur;
    private final ParametresPresence48 parametres;
    private final Clock horloge;

    public SessionService(SessionRepository sessions, PromotionRepository promotions,
            GenerateurCode generateur, ParametresPresence48 parametres, Clock horloge) {
        this.sessions = sessions;
        this.promotions = promotions;
        this.generateur = generateur;
        this.parametres = parametres;
        this.horloge = horloge;
    }

    /**
     * EF1 — ouvre une session et lui attribue un code de presence.
     *
     * @throws ExceptionMetier {@code 404 PROMOTION_INCONNUE} si la promotion
     *     n'existe pas
     */
    @Transactional
    public SessionOuverteDto ouvrir(OuvrirSessionRequete requete) {
        Promotion promotion = promotions.findById(requete.promotionId())
                .orElseThrow(() -> new ExceptionMetier(CodeErreur.PROMOTION_INCONNUE));

        OffsetDateTime ouverture = OffsetDateTime.now(horloge);
        // RG1 : l'expiration decoule de l'ouverture, elle n'est jamais saisie.
        OffsetDateTime expiration = ouverture.plus(parametres.dureeValiditeCode());

        SessionCours session = sessions.save(
                new SessionCours(requete.titre(), promotion, codeLibre(), ouverture, expiration));

        return new SessionOuverteDto(session.getId(), session.getCode(),
                session.getOuvertureAt(), session.getExpirationAt());
    }

    /**
     * EF12 — le formateur cloture une session.
     *
     * <p>C'est l'operation que la demande du client ne contenait pas, alors que
     * Q10, Q12 et Q13 en dependent toutes les trois. Elle est distincte de
     * l'expiration du code (RG22) : une session peut avoir un code expire depuis
     * des heures et rester ouverte aux depots tardifs (Q12).
     *
     * @throws ExceptionMetier {@code 404 SESSION_INCONNUE} ou
     *     {@code 409 SESSION_DEJA_CLOTUREE}
     */
    @Transactional
    public SessionDto cloturer(Long sessionId) {
        SessionCours session = sessions.findById(sessionId)
                .orElseThrow(() -> new ExceptionMetier(CodeErreur.SESSION_INCONNUE));

        if (session.estCloturee()) {
            throw new ExceptionMetier(CodeErreur.SESSION_DEJA_CLOTUREE);
        }

        session.cloturer(OffsetDateTime.now(horloge));
        return versDto(session);
    }

    /** Operation ajoutee : les sessions d'une promotion, la plus recente d'abord. */
    @Transactional(readOnly = true)
    public List<SessionDto> listerParPromotion(Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw new ExceptionMetier(CodeErreur.PROMOTION_INCONNUE);
        }
        return sessions.findByPromotionIdOrderByOuvertureAtDesc(promotionId).stream()
                .map(SessionService::versDto)
                .toList();
    }

    static SessionDto versDto(SessionCours session) {
        return new SessionDto(session.getId(), session.getTitre(),
                session.getPromotion().getId(), session.getCode(),
                session.getOuvertureAt(), session.getExpirationAt(),
                session.getClotureeAt(), session.estCloturee());
    }

    /** RG6 — un code qu'aucune session ne porte deja. */
    private String codeLibre() {
        for (int tirage = 0; tirage < TIRAGES_MAX; tirage++) {
            String code = generateur.nouveauCode();
            if (!sessions.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException(
                "Impossible de tirer un code de presence libre apres " + TIRAGES_MAX + " tentatives");
    }
}
