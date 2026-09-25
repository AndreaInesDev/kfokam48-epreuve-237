package cm.kfokam48.presence48.session;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * EF12 — la cloture de session.
 *
 * <p>C'est l'operation que la demande du client ne contenait pas, alors que Q10,
 * Q12 et Q13 en dependent toutes les trois. Ces tests verifient qu'elle a bien
 * l'effet que RG19 annonce : couper les depots et les rendus.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("EF12 — le formateur cloture une session")
class CloturerSessionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("200, la session est marquee cloturee, et les depots y sont coupes (RG19)")
    void cloturerCoupeLesDepots() throws Exception {
        // Avant la cloture, l'etudiant 6 peut deposer sur la session 2 (RG11).
        mockMvc.perform(post("/api/sessions/2/cloture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cloturee").value(true))
                .andExpect(jsonPath("$.clotureeAt").exists());

        // Apres, le meme depot est refuse.
        mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sessionId\": 2, \"etudiantId\": 6,"
                                + " \"lien\": \"https://github.com/demo/trop-tard\" }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    @DisplayName("la cloture coupe aussi les rendus de relecture (RG19)")
    void cloturerCoupeLesRendus() throws Exception {
        mockMvc.perform(post("/api/sessions/2/cloture")).andExpect(status().isOk());

        mockMvc.perform(post("/api/relectures/7").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"note\": 15, \"commentaire\": \"Trop tard.\" }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    @DisplayName("session deja cloturee : 409 SESSION_DEJA_CLOTUREE")
    void dejaCloturee() throws Exception {
        mockMvc.perform(post("/api/sessions/1/cloture"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_DEJA_CLOTUREE"));
    }

    @Test
    @DisplayName("session inexistante : 404 SESSION_INCONNUE")
    void sessionInconnue() throws Exception {
        mockMvc.perform(post("/api/sessions/9999/cloture"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }

    @Test
    @DisplayName("RG16 — une session cloturee laisse ses exercices non relus visibles")
    void lesExercicesNonRelusRestentVisibles() throws Exception {
        mockMvc.perform(post("/api/sessions/2/cloture")).andExpect(status().isOk());

        // Essomba Clarisse doit toujours ses deux relectures : la cloture est un
        // etat de la session, pas des exercices (D4).
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/tableau").param("promotionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[4].nom").value("Essomba Clarisse"))
                .andExpect(jsonPath("$[4].relecturesEnAttente")
                        .value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }
}
