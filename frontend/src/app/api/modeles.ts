/**
 * Les types de l'API, calqués sur `api/contrat.yaml`.
 *
 * Ils sont écrits à la main plutôt que générés : le contrat est court, et une
 * divergence entre ces types et le contrat casse la compilation TypeScript —
 * c'est la raison pour laquelle Angular a été choisi (voir le README).
 */

export interface Promotion {
  id: number;
  nom: string;
}

export interface Etudiant {
  id: number;
  nom: string;
}

/** Réponse 201 de POST /api/sessions — les quatre champs imposés. */
export interface SessionOuverte {
  id: number;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
}

export interface Session {
  id: number;
  titre: string;
  promotionId: number;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
  clotureeAt: string | null;
  cloturee: boolean;
}

export type SourcePresence = 'ETUDIANT' | 'FORMATEUR';

/** Réponse 201 de POST /api/presences — les quatre champs imposés. */
export interface Presence {
  id: number;
  sessionId: number;
  etudiantId: number;
  source: SourcePresence;
}

export type StatutExercice =
  | 'EN_ATTENTE_ASSIGNATION'
  | 'EN_ATTENTE_RELECTURE'
  | 'EN_COURS_DE_RELECTURE'
  | 'RELU';

/** Réponse 201 de POST /api/exercices — les deux champs imposés. */
export interface ExerciceDepose {
  id: number;
  statut: StatutExercice;
}

/**
 * Une ligne du tableau du formateur.
 *
 * `moyenne` est `number | null` et non `number` : RG18 impose qu'elle vaille
 * `null` — jamais 0 — tant qu'aucune note n'a été reçue. Le type l'oblige donc
 * à être traitée explicitement à l'affichage.
 */
export interface LigneTableau {
  etudiantId: number;
  nom: string;
  presences: number;
  exercicesDeposes: number;
  moyenne: number | null;
  relecturesEnAttente: number;
}

/**
 * Une relecture vue par son relecteur. Aucun champ ne désigne l'auteur : RG14
 * et Q8 l'interdisent, et cette absence est structurelle côté serveur aussi.
 */
export interface RelectureATraiter {
  id: number;
  exerciceId: number;
  lien: string;
  sessionTitre: string;
  commencee: boolean;
}

/** Le format d'erreur imposé, pour TOUTES les erreurs sans exception. */
export interface ErreurApi {
  code: string;
  message: string;
}
