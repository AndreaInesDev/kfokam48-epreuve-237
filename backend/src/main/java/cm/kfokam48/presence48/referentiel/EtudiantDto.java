package cm.kfokam48.presence48.referentiel;

/**
 * Un etudiant tel que l'API l'expose — schema Etudiant du contrat.
 *
 * <p>Volontairement reduit a l'identifiant et au nom : Q1 dit que l'etudiant se
 * choisit dans une liste, sans mot de passe. Il n'y a donc rien d'autre a
 * exposer, et surtout rien de confidentiel.
 */
public record EtudiantDto(Long id, String nom) {
}
