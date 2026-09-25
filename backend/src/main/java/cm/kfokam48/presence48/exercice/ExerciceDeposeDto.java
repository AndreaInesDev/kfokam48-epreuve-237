package cm.kfokam48.presence48.exercice;

import cm.kfokam48.presence48.domaine.StatutExercice;

/**
 * Reponse 201 de {@code POST /api/exercices}, imposee par le contrat :
 * exactement {@code { id, statut }}.
 */
public record ExerciceDeposeDto(Long id, StatutExercice statut) {
}
