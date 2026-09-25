package cm.kfokam48.presence48.relecture;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * EF8 et RG12 — ce que le relecteur doit relire, et l'instant ou sa relecture
 * commence.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("EF8 / RG12 — les relectures a rendre et leur ouverture")
class RelecturesARendreTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/relectures?relecteurId= ne renvoie que les non rendues (RG16)")
    void seulementLesNonRendues() throws Exception {
        // Essomba Clarisse (5) doit deux relectures : la 4, ouverte, et la 7.
        mockMvc.perform(get("/api/relectures").param("relecteurId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].lien").exists())
                .andExpect(jsonPath("$[0].sessionTitre").exists())
                .andExpect(jsonPath("$[0].commencee").exists());
    }

    @Test
    @DisplayName("un relecteur qui a tout rendu recoit une liste vide, pas une erreur")
    void listeVideSiToutRendu() throws Exception {
        // Bikoi Serge (2) n'a qu'une relecture, la 1, deja rendue.
        mockMvc.perform(get("/api/relectures").param("relecteurId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    @DisplayName("la liste ne revele jamais l'identite de l'auteur (RG14, Q8)")
    void aucuneIdentiteDAuteur() throws Exception {
        String reponse = mockMvc.perform(get("/api/relectures").param("relecteurId", "5"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(reponse)
                .doesNotContain("etudiantId", "auteur", "nom");
    }

    @Test
    @DisplayName("etudiant inexistant : 404 ETUDIANT_INCONNU")
    void etudiantInconnu() throws Exception {
        mockMvc.perform(get("/api/relectures").param("relecteurId", "9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
    }

    @Test
    @DisplayName("RG12 — ouvrir marque la relecture commencee, et c'est idempotent")
    void ouvrirEstIdempotent() throws Exception {
        mockMvc.perform(post("/api/relectures/7/ouverture").param("relecteurId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commencee").value(true));

        mockMvc.perform(post("/api/relectures/7/ouverture").param("relecteurId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commencee").value(true));
    }

    @Test
    @DisplayName("ouvrir une relecture qui ne m'est pas assignee : 403 RELECTURE_NON_ASSIGNEE")
    void ouvrirCeQuiNEstPasAMoi() throws Exception {
        mockMvc.perform(post("/api/relectures/7/ouverture").param("relecteurId", "12"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RELECTURE_NON_ASSIGNEE"));
    }

    @Test
    @DisplayName("ouvrir une relecture deja rendue : 409 RELECTURE_DEJA_RENDUE")
    void ouvrirCeQuiEstRendu() throws Exception {
        mockMvc.perform(post("/api/relectures/1/ouverture").param("relecteurId", "2"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
    }
}
