package cm.kfokam48.presence48.domaine;

/**
 * RG15 — l'origine d'une presence, exigee par Q14 : « il faut que ca se voie ».
 *
 * <p>Les deux valeurs sont celles du contrat, et la contrainte
 * {@code ck_presence_source} de la migration les reproduit en base.
 */
public enum SourcePresence {

    /** L'etudiant a saisi le code lui-meme. */
    ETUDIANT,

    /** Le formateur l'a ajoute a la main, telephone en panne (Q14). */
    FORMATEUR
}
