package cm.kfokam48.presence48.erreur;

/**
 * Toute exception métier de Présence48 porte un {@link CodeErreur}.
 *
 * <p>Le service lève cette exception (ou l'une de ses spécialisations à venir),
 * le {@code @RestControllerAdvice} unique la traduit en réponse HTTP : le
 * statut vient du code, le corps est toujours {@code { code, message }} (B4).
 * Aucun contrôleur ne construit lui-même une réponse d'erreur.
 */
public class ExceptionMetier extends RuntimeException {

    private final CodeErreur code;

    public ExceptionMetier(CodeErreur code) {
        this(code, code.message());
    }

    /** Permet un message plus précis que le message par défaut du code. */
    public ExceptionMetier(CodeErreur code, String message) {
        super(message);
        this.code = code;
    }

    public CodeErreur code() {
        return code;
    }
}
