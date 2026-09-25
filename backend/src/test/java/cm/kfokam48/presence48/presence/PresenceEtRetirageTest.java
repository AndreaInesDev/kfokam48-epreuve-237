package cm.kfokam48.presence48.presence;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Bug #34 — une presence valide annulee par le retirage au sort de RG20.
 *
 * <p>Le client l'a decrit ainsi : « ils ont tape le code presque en meme temps et
 * il n'y en a qu'un seul qui apparait dans ma liste ».
 *
 * <p>Deux presences concurrentes sur une meme session retentent toutes les deux
 * le tirage au sort pour le meme exercice orphelin. La seconde viole
 * {@code UNIQUE (exercice_id)} de RG7 et, comme l'insertion de la presence vit
 * dans la meme transaction, elle est annulee avec elle.
 *
 * <p>Ce test ne lance pas deux fils d'execution : il installe directement l'etat
 * que la concurrence produit — un exercice en attente d'assignation qui porte
 * deja une relecture — parce que c'est exactement ce que voit la seconde
 * transaction, et parce qu'un test deterministe vaut mieux qu'un test qui echoue
 * une fois sur dix.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("Bug #34 — le retirage RG20 ne doit jamais annuler une presence valide")
class PresenceEtRetirageTest {

    /** Session 3 du jeu de demonstration : ouverte, code valide, aucune presence. */
    private static final String CODE_VALIDE = "ANGU-SRV3";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void installerLEtatQueLaConcurrenceProduit() {
        // Un exercice orphelin sur la session 3, deja pourvu d'une relecture :
        // c'est l'etat que voit la seconde des deux transactions concurrentes.
        jdbc.update("""
                INSERT INTO exercice (id, session_id, etudiant_id, lien, statut, depose_at)
                VALUES (900, 3, 10, 'https://github.com/demo/course-concurrente',
                        'EN_ATTENTE_ASSIGNATION', CURRENT_TIMESTAMP)
                """);
        jdbc.update("""
                INSERT INTO relecture (id, exercice_id, relecteur_id, assignee_at)
                VALUES (900, 900, 11, CURRENT_TIMESTAMP)
                """);
    }

    @Test
    @DisplayName("un exercice deja assigne n'empeche pas d'enregistrer une presence")
    void unExerciceDejaAssigneNEmpechePasUnePresence() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"" + CODE_VALIDE + "\", \"etudiantId\": 9 }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.etudiantId").value(9))
                .andExpect(jsonPath("$.source").value("ETUDIANT"));

        Long presences = jdbc.queryForObject(
                "SELECT count(*) FROM presence WHERE session_id = 3 AND etudiant_id = 9",
                Long.class);
        org.assertj.core.api.Assertions.assertThat(presences)
                .as("la presence doit survivre au retirage")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("RG7 reste tenue : l'exercice deja assigne ne recoit pas un second relecteur")
    void rg7ResteTenue() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"" + CODE_VALIDE + "\", \"etudiantId\": 12 }"))
                .andExpect(status().isCreated());

        Long relectures = jdbc.queryForObject(
                "SELECT count(*) FROM relecture WHERE exercice_id = 900", Long.class);
        org.assertj.core.api.Assertions.assertThat(relectures)
                .as("RG7 : un seul relecteur par exercice")
                .isEqualTo(1);
    }
}
