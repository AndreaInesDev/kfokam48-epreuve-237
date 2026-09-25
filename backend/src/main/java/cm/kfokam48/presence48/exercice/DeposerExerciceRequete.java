package cm.kfokam48.presence48.exercice;

import jakarta.validation.constraints.NotNull;

/**
 * Corps de {@code POST /api/exercices} — impose par le contrat :
 * {@code { sessionId, etudiantId, lien }}.
 *
 * <p>Le champ {@code lien} n'est volontairement PAS valide par annotation. Le
 * contrat exige pour ce cas le code {@code LIEN_INVALIDE}, alors qu'une
 * violation de contrainte declarative ressort en {@code REQUETE_INVALIDE}.
 * RG10 est donc verifiee dans le service, qui peut nommer le code exact — la
 * conformite au contrat (B2) primant sur l'elegance de la declaration.
 */
public record DeposerExerciceRequete(
        @NotNull(message = "La session est obligatoire.")
        Long sessionId,

        @NotNull(message = "L'etudiant est obligatoire.")
        Long etudiantId,

        String lien) {
}
