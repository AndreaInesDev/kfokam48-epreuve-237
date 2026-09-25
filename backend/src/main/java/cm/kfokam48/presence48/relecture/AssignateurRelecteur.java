package cm.kfokam48.presence48.relecture;

import cm.kfokam48.presence48.domaine.Etudiant;
import cm.kfokam48.presence48.domaine.Exercice;
import cm.kfokam48.presence48.domaine.Relecture;
import cm.kfokam48.presence48.domaine.StatutExercice;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.springframework.stereotype.Service;

/**
 * Tirage au sort du relecteur d'un exercice (EF7).
 *
 * <p>Trois regles se rencontrent ici :
 * <ul>
 *   <li><b>RG8</b> — le relecteur est tire au hasard parmi les etudiants
 *       <i>presents a la session</i>, et par le systeme : ni le formateur ni
 *       l'auteur ne choisissent ;</li>
 *   <li><b>RG2</b> — l'auteur est exclu du tirage, toujours, meme s'il est le
 *       seul present. Cette regle ne peut pas etre tenue par une contrainte de
 *       base : un CHECK ne peut pas comparer {@code relecture.relecteur_id} a
 *       {@code exercice.etudiant_id}, qui vit dans une autre table. C'est donc
 *       ici qu'elle vit, et c'est elle que couvre le test unitaire de B6 ;</li>
 *   <li><b>RG20</b> — si l'ensemble des candidats est vide, aucune relecture
 *       n'est creee et l'exercice reste en attente d'assignation. L'etudiant
 *       n'en sait rien et n'a pas a en savoir : il a bien depose son exercice.</li>
 * </ul>
 */
@Service
public class AssignateurRelecteur {

    private final RelectureRepository relectures;
    private final Random aleatoire;
    private final Clock horloge;

    public AssignateurRelecteur(RelectureRepository relectures, Random aleatoire, Clock horloge) {
        this.relectures = relectures;
        this.aleatoire = aleatoire;
        this.horloge = horloge;
    }

    /**
     * Tente d'assigner un relecteur a un exercice.
     *
     * @param exercice l'exercice a relire
     * @param presentsALaSession les etudiants presents a la session de cet
     *     exercice, auteur inclus — c'est cette methode qui l'exclut (RG2)
     * @return la relecture creee, ou vide si personne n'etait eligible (RG20)
     */
    public Optional<Relecture> assigner(Exercice exercice, List<Etudiant> presentsALaSession) {
        // RG2 : l'auteur ne peut jamais relire son propre exercice.
        List<Etudiant> candidats = presentsALaSession.stream()
                .filter(candidat -> !candidat.getId().equals(exercice.getEtudiant().getId()))
                .toList();

        if (candidats.isEmpty()) {
            // RG20 : rien a tirer. L'exercice attend qu'un present arrive.
            exercice.changerStatut(StatutExercice.EN_ATTENTE_ASSIGNATION);
            return Optional.empty();
        }

        // RG8 : au hasard, sans preference ni ordre.
        Etudiant relecteur = candidats.get(aleatoire.nextInt(candidats.size()));

        Relecture relecture = relectures.save(
                new Relecture(exercice, relecteur, OffsetDateTime.now(horloge)));
        exercice.changerStatut(StatutExercice.EN_ATTENTE_RELECTURE);
        return Optional.of(relecture);
    }
}
