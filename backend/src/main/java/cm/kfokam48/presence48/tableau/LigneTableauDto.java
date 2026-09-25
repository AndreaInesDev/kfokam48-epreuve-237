package cm.kfokam48.presence48.tableau;

/**
 * Une ligne du tableau du formateur — schema impose par le contrat :
 * exactement {@code { etudiantId, nom, presences, exercicesDeposes, moyenne,
 * relecturesEnAttente }}.
 *
 * <p>{@code moyenne} est un {@code Double} et non un {@code double} : RG18 exige
 * qu'elle vaille {@code null}, et non zero, tant qu'aucune note n'a ete recue.
 * Un zero laisserait croire a un travail note zero, ce qui est faux et injuste.
 * Le contrat la declare d'ailleurs {@code nullable}.
 */
public record LigneTableauDto(
        Long etudiantId,
        String nom,
        long presences,
        long exercicesDeposes,
        Double moyenne,
        long relecturesEnAttente) {
}
