package cm.kfokam48.presence48.relecture;

/**
 * Corps de {@code POST /api/relectures/{id}} — impose par le contrat :
 * {@code { note, commentaire }}.
 *
 * <p>La note est un {@code Integer} et non un {@code int} : absente, elle doit
 * ressortir en {@code 400 NOTE_INVALIDE} et non provoquer un zero implicite.
 * Elle n'est pas validee par annotation, parce que le contrat exige pour ce cas
 * le code {@code NOTE_INVALIDE}, la ou une contrainte declarative ressort en
 * {@code REQUETE_INVALIDE} — meme arbitrage que RG10 sur le lien d'un exercice.
 *
 * <p>{@code relecteurId} est un <b>ajout facultatif</b>. Le contrat ne transmet
 * pas l'identite de l'appelant, et Q1 refuse toute authentification : l'API ne
 * peut donc pas savoir qui rend la relecture. Quand le frontend le fournit, le
 * service peut refuser un relecteur qui n'est pas celui assigne
 * ({@code 403 RELECTURE_NON_ASSIGNEE}) ; quand il est absent, le corps impose
 * reste valable tel quel.
 */
public record RendreRelectureRequete(Integer note, String commentaire, Long relecteurId) {
}
