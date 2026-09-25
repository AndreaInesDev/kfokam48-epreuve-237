package cm.kfokam48.presence48.tableau;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
 * EF11 — le tableau recapitulatif du formateur.
 *
 * <p>Contexte neuf avant la classe : les valeurs attendues sont celles du jeu de
 * demonstration, et d'autres tests deposent des exercices qui les modifieraient.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("EF11 — le tableau recapitulatif du formateur")
class TableauControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("200 avec une ligne par etudiant, triee par nom, et les six champs imposes")
    void tableauDeLaPromotion() throws Exception {
        mockMvc.perform(get("/api/tableau").param("promotionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(12)))
                .andExpect(jsonPath("$[0].etudiantId").value(1))
                .andExpect(jsonPath("$[0].nom").value("Abena Marceline"))
                .andExpect(jsonPath("$[0].presences").value(2))
                .andExpect(jsonPath("$[0].exercicesDeposes").value(2))
                .andExpect(jsonPath("$[0].moyenne").value(12.0))
                .andExpect(jsonPath("$[0].relecturesEnAttente").value(0));
    }

    @Test
    @DisplayName("RG18 — un etudiant sans aucune note recue a une moyenne null, jamais 0")
    void moyenneNulleEtNonZero() throws Exception {
        // Djomo Patrick a depose un exercice, dont la relecture est ouverte mais
        // pas rendue : il n'a donc recu aucune note.
        mockMvc.perform(get("/api/tableau").param("promotionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[3].nom").value("Djomo Patrick"))
                .andExpect(jsonPath("$[3].moyenne").isEmpty());
    }

    @Test
    @DisplayName("RG16 — les relectures assignees et non rendues sont comptees (Q11)")
    void relecturesEnAttenteComptees() throws Exception {
        // Essomba Clarisse doit deux relectures : une ouverte, une jamais ouverte.
        mockMvc.perform(get("/api/tableau").param("promotionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[4].nom").value("Essomba Clarisse"))
                .andExpect(jsonPath("$[4].relecturesEnAttente")
                        .value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("un etudiant jamais present apparait quand meme, a zero")
    void etudiantJamaisPresentApparait() throws Exception {
        mockMvc.perform(get("/api/tableau").param("promotionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[8].nom").value("Kamdem Josiane"))
                .andExpect(jsonPath("$[8].presences").value(0))
                .andExpect(jsonPath("$[8].moyenne").isEmpty());
    }

    @Test
    @DisplayName("aucune ligne ne porte de champ hors contrat")
    void aucunChampHorsContrat() throws Exception {
        String reponse = mockMvc.perform(get("/api/tableau").param("promotionId", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(reponse)
                .doesNotContain("promotionId", "source", "lien", "commentaire");
    }

    @Test
    @DisplayName("promotion inexistante : 404 PROMOTION_INCONNUE")
    void promotionInconnue() throws Exception {
        mockMvc.perform(get("/api/tableau").param("promotionId", "9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}
