package cm.kfokam48.presence48;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prouve ENF7 et la contrainte B6 : les migrations Flyway de production sont
 * rejouees telles quelles sur H2 en mode compatibilite PostgreSQL, donc les
 * tests tournent sur un poste vierge, sans PostgreSQL installe ni lance.
 *
 * <p>Ce test verifie aussi que le jeu de demonstration respecte les regles de
 * gestion : des donnees de demonstration qui violeraient RG2 ou RG18 seraient
 * pires que pas de donnees du tout.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Le schema versionne est portable et le jeu de demonstration est coherent")
class MigrationsPortablesTest {

    @Autowired
    private JdbcTemplate jdbc;

    private long compter(String table) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class);
    }

    @Test
    @DisplayName("les deux migrations creent les six tables de D2")
    void lesSixTablesDeD2Existent() {
        assertThat(compter("promotion")).isEqualTo(2);
        assertThat(compter("etudiant")).isEqualTo(20);
        assertThat(compter("session_cours")).isEqualTo(4);
        assertThat(compter("presence")).isEqualTo(19);
        assertThat(compter("exercice")).isEqualTo(10);
        assertThat(compter("relecture")).isEqualTo(9);
    }

    @Test
    @DisplayName("RG1 — l'expiration de chaque session vaut bien l'ouverture plus 15 minutes")
    void toutesLesSessionsExpirentQuinzeMinutesApresLeurOuverture() {
        Long ecarts = jdbc.queryForObject("""
                SELECT count(*) FROM session_cours
                WHERE expiration_at <> ouverture_at + INTERVAL '15' MINUTE
                """, Long.class);
        assertThat(ecarts).as("sessions dont l'expiration ne respecte pas RG1").isZero();
    }

    @Test
    @DisplayName("RG2 — aucun etudiant du jeu de demonstration ne relit son propre exercice")
    void aucuneAutoRelectureDansLesDonneesDeDemonstration() {
        Long autoRelectures = jdbc.queryForObject("""
                SELECT count(*) FROM relecture r
                JOIN exercice x ON x.id = r.exercice_id
                WHERE r.relecteur_id = x.etudiant_id
                """, Long.class);
        assertThat(autoRelectures).as("violations de RG2 dans les donnees de demonstration").isZero();
    }

    @Test
    @DisplayName("RG8 — chaque relecteur etait present a la session de l'exercice qu'il relit")
    void chaqueRelecteurEtaitPresentALaSession() {
        Long relecteursAbsents = jdbc.queryForObject("""
                SELECT count(*) FROM relecture r
                JOIN exercice x ON x.id = r.exercice_id
                WHERE NOT EXISTS (
                    SELECT 1 FROM presence p
                    WHERE p.session_id = x.session_id AND p.etudiant_id = r.relecteur_id)
                """, Long.class);
        assertThat(relecteursAbsents).as("violations de RG8 dans les donnees de demonstration").isZero();
    }

    @Test
    @DisplayName("RG20 — un exercice illustre le cas ou aucun relecteur n'a pu etre tire au sort")
    void unExerciceResteEnAttenteDAssignation() {
        Long sansRelecteur = jdbc.queryForObject("""
                SELECT count(*) FROM exercice x
                WHERE x.statut = 'EN_ATTENTE_ASSIGNATION'
                  AND NOT EXISTS (SELECT 1 FROM relecture r WHERE r.exercice_id = x.id)
                """, Long.class);
        assertThat(sansRelecteur).isEqualTo(1);
    }

    @Test
    @DisplayName("RG18 — au moins un etudiant n'a recu aucune note, sa moyenne doit valoir null")
    void auMoinsUnEtudiantSansAucuneNote() {
        Long sansNote = jdbc.queryForObject("""
                SELECT count(*) FROM etudiant e
                WHERE EXISTS (SELECT 1 FROM exercice x WHERE x.etudiant_id = e.id)
                  AND NOT EXISTS (
                      SELECT 1 FROM relecture r
                      JOIN exercice x2 ON x2.id = r.exercice_id
                      WHERE x2.etudiant_id = e.id AND r.rendue_at IS NOT NULL)
                """, Long.class);
        assertThat(sansNote).isPositive();
    }

    @Test
    @DisplayName("RG15 — le jeu de demonstration contient une presence ajoutee par le formateur")
    void unePresenceAjouteeParLeFormateur() {
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM presence WHERE source = 'FORMATEUR'", Long.class))
                .isPositive();
    }
}
