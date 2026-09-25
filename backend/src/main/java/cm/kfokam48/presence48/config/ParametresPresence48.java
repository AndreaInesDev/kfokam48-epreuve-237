package cm.kfokam48.presence48.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Les regles de gestion chiffrees, lues depuis {@code application.yml} plutot
 * qu'ecrites en dur.
 *
 * <p>RG1 et RG17 portent des durees. Les figer dans le code obligerait a
 * recompiler pour les changer, et surtout empecherait un test de les raccourcir
 * pour verifier l'expiration sans attendre quinze minutes.
 *
 * @param dureeValiditeCode RG1 — duree de validite d'un code de presence
 * @param echecsAvantBlocage RG17 — nombre de codes errones toleres
 * @param dureeBlocage RG17 — duree du blocage apres ces echecs
 */
@ConfigurationProperties(prefix = "presence48")
public record ParametresPresence48(
        Duration dureeValiditeCode,
        int echecsAvantBlocage,
        Duration dureeBlocage) {
}
