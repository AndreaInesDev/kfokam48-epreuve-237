package cm.kfokam48.presence48.erreur;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Prouve que {@link CodeErreur} est bien le miroir du schéma {@code CodeErreur}
 * d'api/contrat.yaml : mêmes codes, mêmes statuts (B2, ENF4).
 *
 * <p>Le contrat est la source unique. Si les deux énumérations divergent —
 * un code ajouté d'un côté sans être ajouté de l'autre, ou un statut changé —
 * ce test casse avant qu'un endpoint ne renvoie la mauvaise réponse.
 *
 * <p>Le test lit le fichier de contrat depuis la racine du dépôt : il tourne
 * donc tel quel sur un poste vierge, sans base ni service lancé (ENF7).
 */
@DisplayName("Le catalogue CodeErreur de l'API est le miroir exact de celui du contrat")
class CodeErreurContratTest {

    /** Une ligne d'enum YAML : « - CODE_EXPIRE   # 410 — RG1 ». */
    private static final Pattern LIGNE_ENUM =
            Pattern.compile("^\\s*-\\s+([A-Z][A-Z0-9_]*)\\s+#\\s*(\\d{3})");

    private static final Path CONTRAT = Path.of("..", "api", "contrat.yaml");

    @Test
    @DisplayName("chaque code du contrat existe en Java, avec le statut exact déclaré")
    void chaqueCodeDuContratExisteAvecSonStatut() throws IOException {
        Map<String, Integer> codesContrat = lireEnumDuContrat();

        assertThat(codesContrat)
                .as("codes lus dans api/contrat.yaml — le fichier est-il bien à sa place ?")
                .isNotEmpty();

        // Côté contrat → côté Java : le code existe, et son statut concorde.
        for (Map.Entry<String, Integer> entree : codesContrat.entrySet()) {
            String nom = entree.getKey();
            assertThat(enumJava(nom))
                    .as("le code %s du contrat n'existe pas dans l'enum Java", nom)
                    .isNotNull();
            assertThat(enumJava(nom).statut().value())
                    .as("statut du code %s : contrat = %d, Java = %d",
                            nom, entree.getValue(), enumJava(nom).statut().value())
                    .isEqualTo(entree.getValue());
        }

        // Côté Java → côté contrat : aucun code inventé en dehors du contrat.
        for (CodeErreur code : CodeErreur.values()) {
            assertThat(codesContrat)
                    .as("le code Java %s n'est déclaré nulle part dans le contrat", code)
                    .containsKey(code.name());
        }
    }

    @Test
    @DisplayName("chaque code porte un message français non vide (ENF8)")
    void chaqueCodeAFrancais() {
        for (CodeErreur code : CodeErreur.values()) {
            assertThat(code.message())
                    .as("message du code %s", code)
                    .isNotBlank()
                    .doesNotContain("TODO")
                    .doesNotMatch(".*\\b(null|undefined|Exception)\\b.*");
        }
    }

    /** Extrait du contrat le bloc {@code CodeErreur} : code → statut annoncé. */
    private Map<String, Integer> lireEnumDuContrat() throws IOException {
        assertThat(Files.isRegularFile(CONTRAT))
                .as("api/contrat.yaml introuvable depuis %s", Path.of("").toAbsolutePath())
                .isTrue();

        Map<String, Integer> codes = new LinkedHashMap<>();
        boolean dansBloc = false;
        for (String ligne : Files.readAllLines(CONTRAT)) {
            if (ligne.matches("^\\s{4}CodeErreur:")) {
                dansBloc = true;
                continue;
            }
            if (dansBloc && ligne.matches("^\\s{4}\\S.*:\\s*$")) {
                break; // prochaine clé du bloc schemas : le catalogue est terminé
            }
            if (dansBloc) {
                Matcher correspondance = LIGNE_ENUM.matcher(ligne);
                if (correspondance.find()) {
                    codes.put(correspondance.group(1), Integer.parseInt(correspondance.group(2)));
                }
            }
        }
        return codes;
    }

    private CodeErreur enumJava(String nom) {
        try {
            return CodeErreur.valueOf(nom);
        } catch (IllegalArgumentException absent) {
            return null;
        }
    }
}
