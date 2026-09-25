package cm.kfokam48.presence48.relecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cm.kfokam48.presence48.domaine.Etudiant;
import cm.kfokam48.presence48.domaine.Exercice;
import cm.kfokam48.presence48.domaine.Relecture;
import cm.kfokam48.presence48.domaine.StatutExercice;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test unitaire de la regle metier la plus importante du projet — RG2 et RG8.
 *
 * <p>C'est le test unitaire exige par la contrainte B6, et il porte sur une
 * regle qui ne peut PAS etre tenue par la base : un CHECK SQL ne peut pas
 * comparer {@code relecture.relecteur_id} a {@code exercice.etudiant_id}, qui
 * vit dans une autre table. Si ce test tombe, l'application viole le principe
 * meme de la relecture entre pairs (Q5 : « Jamais. C'est le principe meme »).
 *
 * <p>Ni Spring ni base : la regle est testee seule.
 */
@DisplayName("RG2 / RG8 / RG20 — tirage au sort du relecteur")
class AssignateurRelecteurTest {

    private static final Clock HORLOGE_FIXE =
            Clock.fixed(Instant.parse("2026-09-25T10:00:00Z"), ZoneOffset.UTC);

    private final RelectureRepository relectures = mock(RelectureRepository.class);

    private AssignateurRelecteur assignateurAvec(long graine) {
        when(relectures.save(any(Relecture.class))).thenAnswer(appel -> appel.getArgument(0));
        return new AssignateurRelecteur(relectures, new Random(graine), HORLOGE_FIXE);
    }

    private Etudiant etudiant(long id) {
        Etudiant etudiant = mock(Etudiant.class);
        when(etudiant.getId()).thenReturn(id);
        return etudiant;
    }

    private Exercice exerciceDe(Etudiant auteur) {
        Exercice exercice = mock(Exercice.class);
        when(exercice.getEtudiant()).thenReturn(auteur);
        return exercice;
    }

    @Test
    @DisplayName("RG2 — l'auteur n'est jamais tire au sort, sur mille tirages")
    void lAuteurNEstJamaisTireAuSort() {
        Etudiant auteur = etudiant(1);
        Etudiant pair = etudiant(2);
        Etudiant autrePair = etudiant(3);

        for (long graine = 0; graine < 1000; graine++) {
            var assignateur = assignateurAvec(graine);
            var relecture = assignateur.assigner(exerciceDe(auteur), List.of(auteur, pair, autrePair));

            assertThat(relecture).isPresent();
            assertThat(relecture.get().getRelecteur().getId())
                    .as("RG2 violee : l'auteur a ete tire au sort (graine %d)", graine)
                    .isNotEqualTo(auteur.getId());
        }
    }

    @Test
    @DisplayName("RG2 — l'auteur seul present : aucune relecture, pas une auto-relecture")
    void auteurSeulPresentNeSAutoRelitPas() {
        Etudiant auteur = etudiant(1);
        Exercice exercice = exerciceDe(auteur);

        var relecture = assignateurAvec(0).assigner(exercice, List.of(auteur));

        assertThat(relecture).isEmpty();
        verify(relectures, never()).save(any());
        verify(exercice).changerStatut(StatutExercice.EN_ATTENTE_ASSIGNATION);
    }

    @Test
    @DisplayName("RG20 — aucun present du tout : l'exercice reste en attente d'assignation")
    void aucunPresentDuTout() {
        Exercice exercice = exerciceDe(etudiant(1));

        assertThat(assignateurAvec(0).assigner(exercice, List.of())).isEmpty();
        verify(exercice).changerStatut(StatutExercice.EN_ATTENTE_ASSIGNATION);
    }

    @Test
    @DisplayName("RG8 — le tirage est aleatoire : plusieurs candidats sortent sur mille graines")
    void leTirageEstBienAleatoire() {
        Etudiant auteur = etudiant(1);
        List<Etudiant> presents = List.of(auteur, etudiant(2), etudiant(3), etudiant(4));

        var tires = new java.util.HashSet<Long>();
        for (long graine = 0; graine < 1000; graine++) {
            assignateurAvec(graine).assigner(exerciceDe(auteur), presents)
                    .ifPresent(r -> tires.add(r.getRelecteur().getId()));
        }
        assertThat(tires)
                .as("le tirage doit pouvoir designer n'importe quel pair present")
                .containsExactlyInAnyOrder(2L, 3L, 4L);
    }

    @Test
    @DisplayName("un seul pair present : c'est lui, et l'exercice passe en attente de relecture")
    void unSeulPairPresent() {
        Etudiant auteur = etudiant(1);
        Etudiant seulPair = etudiant(2);
        Exercice exercice = exerciceDe(auteur);

        var relecture = assignateurAvec(0).assigner(exercice, List.of(auteur, seulPair));

        assertThat(relecture).isPresent();
        assertThat(relecture.get().getRelecteur().getId()).isEqualTo(2L);
        verify(exercice).changerStatut(StatutExercice.EN_ATTENTE_RELECTURE);
    }
}
