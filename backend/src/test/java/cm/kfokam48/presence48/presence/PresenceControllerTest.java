package cm.kfokam48.presence48.presence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * EF3 et EF4 — marquer sa presence, et les cas d'erreur du contrat.
 *
 * <p>C'est le test d'integration exige par la contrainte B6 : il traverse HTTP,
 * le controleur, le service, le repository et la base, et il verifie que les
 * codes de statut sont <b>exactement</b> ceux du contrat et de D3 — y compris
 * les erreurs, qui pesent autant que le cas nominal au bareme.
 *
 * <p>Codes du jeu de demonstration utilises ici :
 * <ul>
 *   <li>{@code ANGU-SRV3} — session ouverte a la creation de la base, code valide ;
 *   <li>{@code SPRG-LAY2} — session ouverte la veille, code expire mais session
 *       non cloturee.
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("EF3 / EF4 — marquer sa presence avec un code")
class PresenceControllerTest {

    private static final String CODE_VALIDE = "ANGU-SRV3";
    private static final String CODE_EXPIRE = "SPRG-LAY2";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper json;

    private String corps(String code, Object etudiantId) {
        return "{ \"code\": \"" + code + "\", \"etudiantId\": " + etudiantId + " }";
    }

    @Test
    @DisplayName("code valide : 201 avec source ETUDIANT (RG15)")
    void casNominal() throws Exception {
        String reponse = mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps(CODE_VALIDE, 9)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.etudiantId").value(9))
                .andExpect(jsonPath("$.source").value("ETUDIANT"))
                .andReturn().getResponse().getContentAsString();

        var noms = new java.util.ArrayList<String>();
        json.readTree(reponse).fieldNames().forEachRemaining(noms::add);
        assertThat(noms)
                .as("le 201 ne doit contenir que les quatre champs du contrat")
                .containsExactlyInAnyOrder("id", "sessionId", "etudiantId", "source");
    }

    @Test
    @DisplayName("code inexistant : 400 CODE_INCONNU")
    void codeInconnu() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps("ZZZZ-9999", 10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODE_INCONNU"));
    }

    @Test
    @DisplayName("code de plus de 15 minutes : 410 CODE_EXPIRE (RG1, RG5)")
    void codeExpire() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps(CODE_EXPIRE, 10)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
    }

    @Test
    @DisplayName("un code expire repond 410 et non 409, meme pour un etudiant deja present")
    void expirationPrimeSurLeDoublon() throws Exception {
        // L'etudiant 1 est deja present a la session SPRG-LAY2 dans le jeu de
        // demonstration. L'ordre de D3 impose 410, pas 409 : sinon la reponse
        // renseignerait sur la validite passee du code.
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps(CODE_EXPIRE, 1)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
    }

    @Test
    @DisplayName("deja present a cette session : 409 DEJA_PRESENT (RG4)")
    void dejaPresent() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps(CODE_VALIDE, 11)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps(CODE_VALIDE, 11)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }

    @Test
    @DisplayName("etudiant inexistant : 404 ETUDIANT_INCONNU")
    void etudiantInconnu() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps(CODE_VALIDE, 99999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
    }

    @Test
    @DisplayName("code absent : 400 REQUETE_INVALIDE, avant toute regle metier")
    void codeAbsent() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"etudiantId\": 12 }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    @DisplayName("aucune reponse d'erreur ne laisse fuir de trace ni de champ Spring")
    void aucuneFuite() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps("ZZZZ-9999", 10)))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.timestamp").doesNotExist())
                .andExpect(jsonPath("$.path").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist());
    }
}
