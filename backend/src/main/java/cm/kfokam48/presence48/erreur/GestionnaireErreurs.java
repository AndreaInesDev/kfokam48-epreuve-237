package cm.kfokam48.presence48.erreur;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Le gestionnaire d'erreurs unique de l'API (B4, ENF4).
 *
 * <p>Toute exception qui traverse l'application — métier, validation, routage,
 * plantage imprévu — sort d'ici sous le format imposé par le contrat :
 *
 * <pre>{ "code": "CODE_EXPIRE", "message": "…" }</pre>
 *
 * <p>Deux garanties, vérifiées par {@code GestionnaireErreursTest} :
 * <ul>
 *   <li>le <b>statut</b> vient toujours de {@link CodeErreur#statut()}, donc du
 *       contrat — jamais d'un choix improvisé dans un contrôleur ;</li>
 *   <li>le corps ne contient que {@code code} et {@code message} : aucune
 *       stack trace, aucun champ de la page d'erreur par défaut de Spring.</li>
 * </ul>
 *
 * <p>Les exceptions métier sont déclarées en premier et plus spécifiques : le
 * discriminateur Spring sélectionne toujours le handler le plus précis, un
 * handler générique ne peut donc pas les dévorer.
 */
@RestControllerAdvice
public class GestionnaireErreurs {

    private static final Logger JOURNAL = LoggerFactory.getLogger(GestionnaireErreurs.class);

    /** Les exceptions métier portent déjà leur code et leur statut. */
    @ExceptionHandler(ExceptionMetier.class)
    public ResponseEntity<ReponseErreur> exceptionMetier(ExceptionMetier ex) {
        return reponse(ex.code(), ex.getMessage());
    }

    // -------------------------------------------------------------------------
    //  Requête invalide — la validation refuse avant le service (400)
    // -------------------------------------------------------------------------

    /**
     * Toute erreur de validation ou de lecture de la requête converge vers
     * {@code 400 REQUETE_INVALIDE} : c'est le seul code que le contrat prévoie
     * pour « champ manquant ou mal formé », quel que soit l'endroit où le défaut
     * a été détecté.
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ReponseErreur> requeteInvalide(Exception ex) {
        return reponse(CodeErreur.REQUETE_INVALIDE, detailChamps(ex));
    }

    /** Message français citant les champs en cause, quand on peut les nommer. */
    private String detailChamps(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException invalide) {
            return champs(invalide.getBindingResult().getFieldErrors());
        }
        if (ex instanceof BindException lie) {
            return champs(lie.getBindingResult().getFieldErrors());
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return "Le corps de la requête est absent ou illisible.";
        }
        if (ex instanceof MissingServletRequestParameterException manquant) {
            return "Le paramètre obligatoire « " + manquant.getParameterName() + " » est absent.";
        }
        if (ex instanceof MethodArgumentTypeMismatchException malTypé) {
            return "La valeur « " + malTypé.getValue() + " » du paramètre « "
                    + malTypé.getName() + " » n'a pas le bon type.";
        }
        return CodeErreur.REQUETE_INVALIDE.message();
    }

    private String champs(List<FieldError> erreurs) {
        if (erreurs.isEmpty()) {
            return CodeErreur.REQUETE_INVALIDE.message();
        }
        String noms = erreurs.stream()
                .map(FieldError::getField)
                .distinct()
                .collect(Collectors.joining(", "));
        return "Champ(s) invalide(s) : " + noms + ".";
    }

    // -------------------------------------------------------------------------
    //  Requête correctement formée mais impossible à servir — erreurs de routage
    //  et de contenu. Sans ce bloc, ces cas retomberaient sur la page d'erreur
    //  par défaut de Spring, que ENF4 interdit.
    // -------------------------------------------------------------------------

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ReponseErreur> routeInconnue(Exception ex) {
        return reponse(CodeErreur.ROUTE_INCONNUE, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ReponseErreur> methodeNonSupportee(HttpRequestMethodNotSupportedException ex) {
        return reponse(CodeErreur.METHODE_NON_SUPPORTEE,
                "La méthode " + ex.getMethod() + " n'est pas supportée pour cette route.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ReponseErreur> mediaNonTraitable(HttpMediaTypeNotSupportedException ex) {
        return reponse(CodeErreur.MEDIA_NON_TRAITABLE, null);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ReponseErreur> mediaNonAccepte(HttpMediaTypeNotAcceptableException ex) {
        return reponse(CodeErreur.MEDIA_NON_ACCEPTE, null);
    }

    // -------------------------------------------------------------------------
    //  Dernier rempart : tout ce qui n'était pas prévu. La trace est journalisée
    //  côté serveur, jamais renvoyée au client (B4).
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ReponseErreur> erreurInterne(Exception ex) {
        JOURNAL.error("Erreur non prévue : {}", ex.getClass().getName(), ex);
        return reponse(CodeErreur.ERREUR_INTERNE, null);
    }

    private ResponseEntity<ReponseErreur> reponse(CodeErreur code, String message) {
        String libelle = (message == null || message.isBlank()) ? code.message() : message;
        return ResponseEntity.status(code.statut()).body(new ReponseErreur(code.name(), libelle));
    }
}
