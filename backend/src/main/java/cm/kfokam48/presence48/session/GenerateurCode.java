package cm.kfokam48.presence48.session;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Fabrique les codes de presence — RG6 : uniques et non devinables.
 *
 * <p>Q4 explique pourquoi : « sinon ils vont deviner les codes entre eux ». Un
 * compteur incremental ou un code derive de l'identifiant de session serait donc
 * disqualifie, meme unique. On tire au hasard cryptographique.
 *
 * <p>L'alphabet exclut O, 0, I, 1 et L : un code se lit a voix haute dans une
 * salle de cours, et une confusion de caractere se paie en tentatives ratees —
 * qui declenchent le blocage de RG17.
 */
@Component
public class GenerateurCode {

    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int LONGUEUR_BLOC = 4;

    private final SecureRandom aleatoire = new SecureRandom();

    /** Un code de la forme {@code ABCD-2345}, neuf caracteres. */
    public String nouveauCode() {
        return bloc() + "-" + bloc();
    }

    private String bloc() {
        StringBuilder bloc = new StringBuilder(LONGUEUR_BLOC);
        for (int i = 0; i < LONGUEUR_BLOC; i++) {
            bloc.append(ALPHABET.charAt(aleatoire.nextInt(ALPHABET.length())));
        }
        return bloc.toString();
    }
}
