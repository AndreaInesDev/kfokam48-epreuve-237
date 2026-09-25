package cm.kfokam48.presence48.erreur;

/**
 * Corps d'erreur imposé par le contrat, pour TOUTES les erreurs sans exception :
 *
 * <pre>{ "code": "CODE_EXPIRE", "message": "Le code de présence a expiré." }</pre>
 *
 * <p>Record volontairement fermé : il ne contient que {@code code} et
 * {@code message}. Ni timestamp, ni statut, ni chemin, ni trace — les champs de
 * la page d'erreur par défaut de Spring ne sortent jamais de l'API (ENF4).
 */
public record ReponseErreur(String code, String message) {
}
