package cm.kfokam48.presence48.erreur;

import org.springframework.http.HttpStatus;

/**
 * Catalogue ferme des codes d'erreur — miroir exact du schéma {@code CodeErreur}
 * d'{@code api/contrat.yaml}.
 *
 * <p>C'est la source unique du couple (code, statut) : deux endpoints ne peuvent
 * pas renvoyer deux orthographes du même code ni deux statuts pour le même cas,
 * parce qu'ils passent tous par cette énumération (B2, ENF4). Le test
 * {@code CodeErreurContratTest} vérifie la correspondance avec le contrat, code
 * par code et statut par statut.
 *
 * <p>Chaque constant porte aussi son message par défaut, en français (ENF8).
 * Un message plus spécifique peut être fourni à la construction de
 * {@link ExceptionMetier}.
 */
public enum CodeErreur {

    // ---- Cas métier : les vingt codes du contrat ---------------------------

    REQUETE_INVALIDE(HttpStatus.BAD_REQUEST,
            "La requête est invalide : un champ est manquant ou mal formé."),
    PROMOTION_INCONNUE(HttpStatus.NOT_FOUND,
            "Cette promotion n'existe pas."),
    SESSION_INCONNUE(HttpStatus.NOT_FOUND,
            "Cette session n'existe pas."),
    ETUDIANT_INCONNU(HttpStatus.NOT_FOUND,
            "Cet étudiant n'existe pas."),
    EXERCICE_INCONNU(HttpStatus.NOT_FOUND,
            "Cet exercice n'existe pas."),
    RELECTURE_INCONNUE(HttpStatus.NOT_FOUND,
            "Cette relecture n'existe pas."),
    CODE_INCONNU(HttpStatus.BAD_REQUEST,
            "Aucune session ne porte ce code de présence."),
    CODE_EXPIRE(HttpStatus.GONE,
            "Le code de présence a expiré."),
    DEJA_PRESENT(HttpStatus.CONFLICT,
            "Cet étudiant est déjà présent à cette session."),
    TROP_D_ESSAIS(HttpStatus.TOO_MANY_REQUESTS,
            "Trop de codes erronés : réessayez dans deux minutes."),
    LIEN_INVALIDE(HttpStatus.BAD_REQUEST,
            "Le lien de l'exercice doit être une URL http ou https valide."),
    EXERCICE_DEJA_DEPOSE(HttpStatus.CONFLICT,
            "Un exercice a déjà été déposé pour cette session."),
    ETUDIANT_NON_PRESENT(HttpStatus.CONFLICT,
            "Cet étudiant n'était pas présent à cette session."),
    SESSION_CLOTUREE(HttpStatus.CONFLICT,
            "Cette session est clôturée : plus aucun dépôt n'y est accepté."),
    SESSION_DEJA_CLOTUREE(HttpStatus.CONFLICT,
            "Cette session est déjà clôturée."),
    RELECTURE_COMMENCEE(HttpStatus.CONFLICT,
            "Cette relecture a déjà commencé : le lien n'est plus modifiable."),
    NOTE_INVALIDE(HttpStatus.BAD_REQUEST,
            "La note doit être un nombre entier compris entre 0 et 20."),
    AUTO_RELECTURE(HttpStatus.FORBIDDEN,
            "Un étudiant ne peut jamais relire son propre exercice."),
    RELECTURE_NON_ASSIGNEE(HttpStatus.FORBIDDEN,
            "Cette relecture n'est pas assignée à cet étudiant."),
    RELECTURE_DEJA_RENDUE(HttpStatus.CONFLICT,
            "Cette relecture a déjà été rendue."),

    // ---- Erreurs hors cas métier (ajout issue #10, cahier v1.3) ------------
    // Le contrat décrit les cas métier. ENF4, elle, couvre TOUTE erreur :
    // route inconnue, verbe refusé, contenu non traitable, plantage imprévu.
    // Chaque cas reçoit donc aussi un code, sinon l'API tomberait sur la page
    // d'erreur par défaut de Spring, ce que la même ENF4 interdit.

    ROUTE_INCONNUE(HttpStatus.NOT_FOUND,
            "Aucune route ne correspond à cette adresse."),
    METHODE_NON_SUPPORTEE(HttpStatus.METHOD_NOT_ALLOWED,
            "Cette méthode n'est pas supportée pour cette route."),
    MEDIA_NON_ACCEPTE(HttpStatus.NOT_ACCEPTABLE,
            "Le type de réponse demandé n'est pas supporté."),
    MEDIA_NON_TRAITABLE(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Le type de contenu envoyé n'est pas supporté."),
    ERREUR_INTERNE(HttpStatus.INTERNAL_SERVER_ERROR,
            "Une erreur interne est survenue. L'incident est journalisé côté serveur.");

    private final HttpStatus statut;
    private final String message;

    CodeErreur(HttpStatus statut, String message) {
        this.statut = statut;
        this.message = message;
    }

    /** Le statut HTTP exact que ce code doit provoquer, d'après le contrat. */
    public HttpStatus statut() {
        return statut;
    }

    /** Message par défaut, en français (ENF8). */
    public String message() {
        return message;
    }
}
