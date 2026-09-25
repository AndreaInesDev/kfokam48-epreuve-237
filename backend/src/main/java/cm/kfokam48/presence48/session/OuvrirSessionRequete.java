package cm.kfokam48.presence48.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corps de {@code POST /api/sessions} — impose par le contrat :
 * {@code { titre, promotionId }}, les deux obligatoires.
 *
 * <p>La validation se fait ici, avant le service (B4) : un champ manquant
 * ressort en {@code 400 REQUETE_INVALIDE} sans qu'aucune regle metier ne soit
 * evaluee.
 */
public record OuvrirSessionRequete(
        @NotBlank(message = "Le titre de la session est obligatoire.")
        @Size(max = 200, message = "Le titre ne peut pas depasser 200 caracteres.")
        String titre,

        @NotNull(message = "La promotion est obligatoire.")
        Long promotionId) {
}
