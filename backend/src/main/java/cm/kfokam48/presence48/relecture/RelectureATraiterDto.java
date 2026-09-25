package cm.kfokam48.presence48.relecture;

/**
 * Une relecture vue par son relecteur — schema {@code RelectureATraiter} du
 * contrat.
 *
 * <p>Ne contient <b>pas</b> l'identite de l'auteur, et ce n'est pas un filtrage
 * d'affichage : elle n'est pas necessaire pour relire un travail, et la faire
 * remonter jusqu'au frontend serait la rendre disponible a qui lit le reseau.
 */
public record RelectureATraiterDto(
        Long id,
        Long exerciceId,
        String lien,
        String sessionTitre,
        boolean commencee) {
}
