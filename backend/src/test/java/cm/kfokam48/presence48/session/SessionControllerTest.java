package cm.kfokam48.presence48.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * EF1 — le formateur ouvre une session et obtient un code de presence.
 * Operation imposee par le contrat : {@code POST /api/sessions}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("EF1 — ouvrir une session de cours")
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper json;

    @Test
    @DisplayName("201 avec les quatre champs imposes, et RG1 : expiration = ouverture + 15 min")
    void ouvrirUneSession() throws Exception {
        String reponse = mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "titre": "Java — les records", "promotionId": 1 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.code").isString())
                .andExpect(jsonPath("$.ouvertureAt").isString())
                .andExpect(jsonPath("$.expirationAt").isString())
                .andReturn().getResponse().getContentAsString();

        var corps = json.readTree(reponse);
        OffsetDateTime ouverture = OffsetDateTime.parse(corps.get("ouvertureAt").asText());
        OffsetDateTime expiration = OffsetDateTime.parse(corps.get("expirationAt").asText());

        assertThat(Duration.between(ouverture, expiration))
                .as("RG1 — le code doit expirer quinze minutes apres l'ouverture")
                .isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("la reponse 201 ne contient QUE les quatre champs du contrat")
    void aucunChampHorsContrat() throws Exception {
        String reponse = mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"titre\": \"Spring — les DTO\", \"promotionId\": 1 }"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var noms = new java.util.ArrayList<String>();
        json.readTree(reponse).fieldNames().forEachRemaining(noms::add);
        assertThat(noms).containsExactlyInAnyOrder("id", "code", "ouvertureAt", "expirationAt");
    }

    @Test
    @DisplayName("deux sessions ouvertes de suite ne portent jamais le meme code (RG6)")
    void deuxSessionsDeuxCodes() throws Exception {
        String premier = codeDUneNouvelleSession();
        String second = codeDUneNouvelleSession();
        assertThat(premier).isNotEqualTo(second);
    }

    @Test
    @DisplayName("titre manquant : 400 REQUETE_INVALIDE citant le champ")
    void titreManquant() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"promotionId\": 1 }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("titre")));
    }

    @Test
    @DisplayName("promotion manquante : 400 REQUETE_INVALIDE")
    void promotionManquante() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"titre\": \"Sans promotion\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    @DisplayName("promotion inexistante : 404 PROMOTION_INCONNUE")
    void promotionInexistante() throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"titre\": \"Promotion fantome\", \"promotionId\": 9999 }"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }

    @Test
    @DisplayName("GET /api/sessions?promotionId= renvoie les sessions, la plus recente d'abord")
    void listerLesSessions() throws Exception {
        mockMvc.perform(get("/api/sessions").param("promotionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].cloturee").exists());
    }

    @Test
    @DisplayName("GET /api/sessions sur une promotion inconnue : 404 PROMOTION_INCONNUE")
    void listerSurPromotionInconnue() throws Exception {
        mockMvc.perform(get("/api/sessions").param("promotionId", "9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }

    private String codeDUneNouvelleSession() throws Exception {
        String reponse = mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"titre\": \"Session de test\", \"promotionId\": 1 }"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(reponse).get("code").asText();
    }
}
