package cm.kfokam48.presence48.referentiel;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * EF2 — le referentiel que Q1 suppose sans que le contrat le fournisse.
 *
 * <p>Test d'integration de bout en bout : HTTP, service, repository, base H2
 * avec les migrations reelles. C'est l'un des deux tests exiges par B6.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("EF2 — l'etudiant se choisit dans la liste de sa promotion")
class ReferentielControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/promotions renvoie 200 et les promotions du jeu de demonstration")
    void listerLesPromotions() throws Exception {
        mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].nom").exists());
    }

    @Test
    @DisplayName("GET /api/promotions/1/etudiants renvoie 200 et les 12 etudiants, tries par nom")
    void listerLesEtudiantsDUnePromotion() throws Exception {
        mockMvc.perform(get("/api/promotions/1/etudiants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(12)))
                .andExpect(jsonPath("$[0].nom").value("Abena Marceline"))
                .andExpect(jsonPath("$[11].nom").value("Ndongo Alain"));
    }

    @Test
    @DisplayName("un etudiant n'expose que son identifiant et son nom — rien d'autre (Q1)")
    void aucunChampSuperfluSurUnEtudiant() throws Exception {
        mockMvc.perform(get("/api/promotions/1/etudiants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].promotion").doesNotExist())
                .andExpect(jsonPath("$[0].promotionId").doesNotExist());
    }

    @Test
    @DisplayName("promotion inexistante : 404 PROMOTION_INCONNUE au format impose")
    void promotionInconnue() throws Exception {
        mockMvc.perform(get("/api/promotions/9999/etudiants"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
