package cm.kfokam48.presence48.exercice;

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
 * EF5 et EF6 — deposer le lien de son exercice.
 *
 * <p>Jeu de demonstration : session 2 (SPRG-LAY2) a un code expire mais n'est
 * PAS cloturee, session 1 est cloturee. Les etudiants 4, 5 et 6 sont presents a
 * la session 2 sans y avoir depose d'exercice.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("EF5 / EF6 — deposer le lien de son exercice")
class ExerciceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String corps(long sessionId, long etudiantId, String lien) {
        return "{ \"sessionId\": " + sessionId + ", \"etudiantId\": " + etudiantId
                + ", \"lien\": \"" + lien + "\" }";
    }

    @Test
    @DisplayName("RG11 — code expire mais session non cloturee : le depot est accepte (Q12)")
    void depotTardifAccepte() throws Exception {
        mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(2, 4, "https://github.com/demo/tardif-djomo")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.statut").exists());
    }

    @Test
    @DisplayName("le 201 ne contient QUE les deux champs du contrat")
    void aucunChampHorsContrat() throws Exception {
        String reponse = mockMvc.perform(post("/api/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps(2, 5, "https://github.com/demo/tardif-essomba")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(reponse)
                .contains("\"id\"", "\"statut\"")
                .doesNotContain("lien", "sessionId", "etudiantId");
    }

    @Test
    @DisplayName("lien qui n'est pas une URL http(s) : 400 LIEN_INVALIDE (RG10)")
    void lienInvalide() throws Exception {
        mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(2, 6, "pas-une-url")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
    }

    @Test
    @DisplayName("exercice deja depose pour cette session : 409 EXERCICE_DEJA_DEPOSE (RG9)")
    void exerciceDejaDepose() throws Exception {
        mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(2, 1, "https://github.com/demo/doublon")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EXERCICE_DEJA_DEPOSE"));
    }

    @Test
    @DisplayName("session cloturee : 409 SESSION_CLOTUREE (RG19)")
    void sessionCloturee() throws Exception {
        mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(1, 7, "https://github.com/demo/trop-tard")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    @DisplayName("etudiant absent de la session : 409 ETUDIANT_NON_PRESENT (RG23)")
    void etudiantNonPresent() throws Exception {
        mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(2, 12, "https://github.com/demo/absent")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ETUDIANT_NON_PRESENT"));
    }

    @Test
    @DisplayName("session inconnue : 404 SESSION_INCONNUE")
    void sessionInconnue() throws Exception {
        mockMvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(9999, 1, "https://github.com/demo/fantome")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }
}
