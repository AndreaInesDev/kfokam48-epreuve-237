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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * EF9 et EF10 — rendre une note entiere sur 20 et un commentaire.
 *
 * <p>Jeu de demonstration : la relecture 5 est assignee a l'etudiant 6 et n'a
 * jamais ete ouverte ; la relecture 1 est deja rendue ; la relecture 7 porte sur
 * un exercice de la session 2, non cloturee ; la relecture 8 porte sur un
 * exercice de la session 4, cloturee.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("EF9 / EF10 — rendre une relecture")
class RelectureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String corps(Object note, String commentaire) {
        return "{ \"note\": " + note + ", \"commentaire\": \"" + commentaire + "\" }";
    }

    @Test
    @DisplayName("note valide : 200 sans corps, et la note entre dans la moyenne (RG18)")
    void rendreUneRelecture() throws Exception {
        // Relecture 7 : exercice 7 de la session 2, qui n'est PAS cloturee.
        // La relecture 5 porterait sur la session 1, cloturee : RG19 la refuse.
        mockMvc.perform(post("/api/relectures/7").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(14, "Bon travail, la lisibilite peut progresser.")))
                .andExpect(status().isOk());

        // Bikoi Serge, auteur de l'exercice 7, avait deja 12 sur l'exercice 2.
        mockMvc.perform(get("/api/tableau").param("promotionId", "1"))
                .andExpect(jsonPath("$[1].nom").value("Bikoi Serge"))
                .andExpect(jsonPath("$[1].moyenne").value(13.0));
    }

    @Test
    @DisplayName("RG13 — relecture deja rendue : 409 RELECTURE_DEJA_RENDUE (Q15 sur Q10)")
    void relectureDejaRendue() throws Exception {
        mockMvc.perform(post("/api/relectures/1").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(20, "Je change d'avis.")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
    }

    @Test
    @DisplayName("RG3 — note hors 0-20 : 400 NOTE_INVALIDE")
    void noteHorsBornes() throws Exception {
        mockMvc.perform(post("/api/relectures/7").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(21, "Trop genereux.")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));

        mockMvc.perform(post("/api/relectures/7").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(-1, "Trop severe.")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
    }

    @Test
    @DisplayName("RG3 — note non entiere : 400, jamais un arrondi silencieux")
    void noteNonEntiere() throws Exception {
        mockMvc.perform(post("/api/relectures/7").contentType(MediaType.APPLICATION_JSON)
                        .content(corps("12.5", "Entre les deux.")))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code")
                        .value(org.hamcrest.Matchers.oneOf("NOTE_INVALIDE", "REQUETE_INVALIDE")));
    }

    @Test
    @DisplayName("note absente : 400 NOTE_INVALIDE, pas un zero implicite")
    void noteAbsente() throws Exception {
        mockMvc.perform(post("/api/relectures/7").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"commentaire\": \"Sans note.\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
    }

    @Test
    @DisplayName("commentaire vide : 400 REQUETE_INVALIDE")
    void commentaireVide() throws Exception {
        mockMvc.perform(post("/api/relectures/7").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(15, "   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }

    @Test
    @DisplayName("un autre etudiant que le relecteur assigne : 403 RELECTURE_NON_ASSIGNEE")
    void relecteurNonAssigne() throws Exception {
        mockMvc.perform(post("/api/relectures/7").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"note\": 15, \"commentaire\": \"Pas mon travail.\","
                                + " \"relecteurId\": 12 }"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("RELECTURE_NON_ASSIGNEE"));
    }

    @Test
    @DisplayName("RG19 — session cloturee : 409 SESSION_CLOTUREE")
    void sessionCloturee() throws Exception {
        mockMvc.perform(post("/api/relectures/9").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(15, "Trop tard, la session est close.")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_CLOTUREE"));
    }

    @Test
    @DisplayName("relecture inexistante : 404 RELECTURE_INCONNUE")
    void relectureInconnue() throws Exception {
        mockMvc.perform(post("/api/relectures/9999").contentType(MediaType.APPLICATION_JSON)
                        .content(corps(15, "Dans le vide.")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RELECTURE_INCONNUE"));
    }
}
