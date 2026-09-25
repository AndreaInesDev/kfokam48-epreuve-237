package cm.kfokam48.presence48.referentiel;

/**
 * Une promotion telle que l'API l'expose. Aucune entite JPA n'est serialisee en
 * JSON (contrainte B3) : ce record est le schema Promotion du contrat.
 */
public record PromotionDto(Long id, String nom) {
}
