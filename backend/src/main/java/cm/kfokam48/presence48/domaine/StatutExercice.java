package cm.kfokam48.presence48.domaine;

/**
 * Cycle de vie d'un exercice — {@code docs/diagrammes/D4-etats-exercice.md}.
 *
 * <p>DEPOSE n'y figure pas : c'est un etat de passage, jamais ecrit en base. A
 * la fin du POST, l'exercice est deja soit en attente d'assignation, soit en
 * attente de relecture.
 */
public enum StatutExercice {

    /** RG20 — depose, mais aucun present n'etait eligible au tirage au sort. */
    EN_ATTENTE_ASSIGNATION,

    /** Un relecteur est assigne, il n'a pas encore ouvert l'exercice. */
    EN_ATTENTE_RELECTURE,

    /** RG12 — le relecteur a ouvert l'exercice : le lien n'est plus modifiable. */
    EN_COURS_DE_RELECTURE,

    /** RG13 — etat terminal, la note est definitive. */
    RELU
}
