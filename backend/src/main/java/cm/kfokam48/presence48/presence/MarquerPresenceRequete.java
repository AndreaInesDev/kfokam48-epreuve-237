package cm.kfokam48.presence48.presence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Corps de {@code POST /api/presences} — impose par le contrat :
 * {@code { code, etudiantId }}, les deux obligatoires.
 */
public record MarquerPresenceRequete(
        @NotBlank(message = "Le code de presence est obligatoire.")
        String code,

        @NotNull(message = "L'etudiant est obligatoire.")
        Long etudiantId) {
}
