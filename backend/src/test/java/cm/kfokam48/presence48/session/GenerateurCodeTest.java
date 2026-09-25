package cm.kfokam48.presence48.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test unitaire de RG6 — le code de presence est unique et non devinable.
 *
 * <p>Pas de Spring, pas de base : une regle metier pure.
 */
@DisplayName("RG6 — le code de presence n'est pas devinable")
class GenerateurCodeTest {

    private final GenerateurCode generateur = new GenerateurCode();

    @Test
    @DisplayName("mille tirages ne produisent aucun doublon")
    void milleTiragesSansDoublon() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codes.add(generateur.nouveauCode());
        }
        assertThat(codes).hasSize(1000);
    }

    @Test
    @DisplayName("deux codes consecutifs ne se deduisent pas l'un de l'autre")
    void deuxCodesConsecutifsSansLienVisible() {
        String premier = generateur.nouveauCode();
        String second = generateur.nouveauCode();
        assertThat(premier).isNotEqualTo(second);
        // Un compteur incremental partagerait tous ses caracteres sauf un.
        long communs = 0;
        for (int i = 0; i < premier.length(); i++) {
            if (premier.charAt(i) == second.charAt(i)) {
                communs++;
            }
        }
        assertThat(communs)
                .as("codes trop proches, Q4 serait en danger : %s puis %s", premier, second)
                .isLessThan(premier.length() - 1);
    }

    @Test
    @DisplayName("le code exclut les caracteres qu'on confond a l'oral : O, 0, I, 1, L")
    void aucunCaractereAmbigu() {
        for (int i = 0; i < 200; i++) {
            assertThat(generateur.nouveauCode()).doesNotContainAnyWhitespaces()
                    .matches("[ABCDEFGHJKMNPQRSTUVWXYZ23456789]{4}-[ABCDEFGHJKMNPQRSTUVWXYZ23456789]{4}");
        }
    }
}
