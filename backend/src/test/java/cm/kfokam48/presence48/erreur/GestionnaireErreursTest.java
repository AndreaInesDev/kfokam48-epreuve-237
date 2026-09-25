package cm.kfokam48.presence48.erreur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test d'intégration exigé par B6 et critère d'ENF4 : on provoque, à travers
 * toute la pile MVC (contrôleur → advice → sérialisation), chaque cas d'erreur
 * du contrat, et on vérifie deux choses à chaque fois :
 *
 * <ul>
 *   <li>le <b>statut HTTP</b> est exactement celui du contrat — y compris le
 *       410 et le 409 ;</li>
 *   <li>le corps contient <b>exactement</b> {@code code} et {@code message},
 *       en français, sans trace ni champ de la page d'erreur Spring.</li>
 * </ul>
 *
 * <p>Les contrôleurs de ce test vivent uniquement dans la classepath de test :
 * ils existent pour provoquer les erreurs que les contrôleurs métier
 * déclencheront au fil des issues #1 à #9, sans dépendre de leur ordre
 * d'arrivée. Le service métier lui-même n'est pas impliqué : c'est le
 * traitement centralisé des erreurs qui est sous test ici.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GestionnaireErreursTest.ControleursDErreur.class)
@DisplayName("Toute erreur sort au format { code, message } avec le statut exact du contrat")
class GestionnaireErreursTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper json;

    // -------------------------------------------------------------------------
    //  1. Chaque code du catalogue → son statut exact, son format exact
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @EnumSource(CodeErreur.class)
    @DisplayName("provoquer ce code renvoie son statut de contrat et un corps {code, message}")
    void chaqueCodeASonStatutEtSonFormat(CodeErreur code) throws Exception {
        MvcResult resultat = mockMvc.perform(get("/test/erreur").param("code", code.name()))
                .andExpect(status().is(code.statut().value()))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(code.name()))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andReturn();

        // Exactement deux champs : ni timestamp, ni status, ni path, ni trace —
        // les marqueurs de la page d'erreur par défaut de Spring.
        var corps = json.readTree(resultat.getResponse().getContentAsString());
        assertThat(corpus(corps))
                .as("le corps ne doit contenir que code et message")
                .containsExactlyInAnyOrder("code", "message");
        assertThat(corps.get("message").asText())
                .as("message du code %s, en français", code)
                .doesNotMatch(".*\\b(NullPointerException|Exception|at cm\\.)\\b.*");
    }

    @Test
    @DisplayName("les vingt codes métier du contrat sont bien présents dans le catalogue")
    void lesVingtCodesMetierSontCouverts() {
        // Garde-fou : si quelqu'un retire un code du contrat, ce test casse.
        long codesMetier = Stream.of(CodeErreur.values())
                .filter(c -> c != CodeErreur.ROUTE_INCONNUE
                        && c != CodeErreur.METHODE_NON_SUPPORTEE
                        && c != CodeErreur.MEDIA_NON_TRAITABLE
                        && c != CodeErreur.MEDIA_NON_ACCEPTE
                        && c != CodeErreur.ERREUR_INTERNE)
                .count();
        assertThat(codesMetier).isEqualTo(20);
    }

    // -------------------------------------------------------------------------
    //  2. Les cas que n'importe quel client peut provoquer sans contrôleurs métier
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("route inconnue : 404 ROUTE_INCONNUE, jamais la page Spring")
    void routeInconnue() throws Exception {
        mockMvc.perform(get("/api/une-route-qui-nexiste-pas"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROUTE_INCONNUE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("méthode non supportée : 405 METHODE_NON_SUPPORTEE")
    void methodeNonSupportee() throws Exception {
        mockMvc.perform(post("/test/erreur").param("code", "CODE_EXPIRE"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHODE_NON_SUPPORTEE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("corps illisible : 400 REQUETE_INVALIDE")
    void corpsIllisible() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ ce n'est pas du json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("champ de validation vide : 400 REQUETE_INVALIDE citant le champ, avant le service")
    void champInvalide() throws Exception {
        MvcResult resultat = mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"))
                .andReturn();

        assertThat(resultat.getResponse().getContentAsString())
                .as("le message doit nommer le champ en faute")
                .contains("titre");
        assertThat(ControleursDErreur.serviceAppele)
                .as("la validation doit refuser avant le service")
                .isFalse();
    }

    @Test
    @DisplayName("type de contenu refusé : 415 MEDIA_NON_TRAITABLE")
    void mediaNonTraitable() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<requete/>"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("MEDIA_NON_TRAITABLE"));
    }

    @Test
    @DisplayName("exception imprévue : 500 ERREUR_INTERNE sans aucune trace dans la réponse")
    void erreurInterne() throws Exception {
        MvcResult resultat = mockMvc.perform(get("/test/erreur").param("code", "PLANTAGE"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERREUR_INTERNE"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andReturn();

        String corps = resultat.getResponse().getContentAsString();
        assertThat(corps)
                .as("aucune stack trace ni détail interne ne doit fuiter")
                .doesNotContain("NullPointerException")
                .doesNotContain("at cm.kfokam48")
                .doesNotContain("java.lang");
    }

    @Test
    @DisplayName("aucune réponse d'erreur ne contient les champs de la page Spring")
    void aucunePageSpringParDefaut() throws Exception {
        // Les cinq familles d'erreur, dans un seul test de non-régression.
        Stream.of(
                        get("/api/route-absente"),
                        get("/test/erreur").param("code", "PLANTAGE"),
                        get("/test/erreur").param("code", "CODE_EXPIRE"),
                        post("/test/validation").contentType(MediaType.APPLICATION_XML).content("<x/>"))
                .forEach(requete -> {
                    try {
                        var corps = json.readTree(
                                mockMvc.perform(requete).andReturn().getResponse().getContentAsString());
                        assertThat(corpus(corps))
                                .as("corps : %s", corps)
                                .containsExactlyInAnyOrder("code", "message");
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                });
    }

    /** Les noms des champs presents dans le corps d'erreur, et rien d'autre. */
    private static java.util.List<String> corpus(com.fasterxml.jackson.databind.JsonNode corps) {
        var noms = new java.util.ArrayList<String>();
        corps.fieldNames().forEachRemaining(noms::add);
        return noms;
    }

    // -------------------------------------------------------------------------
    //  Contrôleurs de test : ils n'existent que pour provoquer les erreurs.
    // -------------------------------------------------------------------------

    @TestConfiguration
    static class ControleursDErreur {

        static boolean serviceAppele;

        @Bean
        ControleurDErreur controleurDErreur() {
            return new ControleurDErreur();
        }
    }

    @RestController
    static class ControleurDErreur {

        /** Un cas par famille d'erreur : routage, validation, plantage. */
        @GetMapping("/test/erreur")
        String erreur(@org.springframework.web.bind.annotation.RequestParam("code") String code) {
            if ("PLANTAGE".equals(code)) {
                throw new NullPointerException("détail interne qui ne doit pas fuiter");
            }
            throw new ExceptionMetier(CodeErreur.valueOf(code));
        }

        @PostMapping("/test/validation")
        String validation(@Valid @RequestBody RequeteDeTest requete) {
            ControleursDErreur.serviceAppele = true;
            return "ok";
        }
    }

    record RequeteDeTest(@NotBlank(message = "Le titre est obligatoire.") String titre) {
    }
}
