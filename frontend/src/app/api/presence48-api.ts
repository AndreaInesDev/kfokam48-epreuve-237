import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import {
  ErreurApi,
  Etudiant,
  ExerciceDepose,
  LigneTableau,
  Presence,
  Promotion,
  RelectureATraiter,
  Session,
  SessionOuverte,
} from './modeles';

/**
 * La seule porte d'entrée vers l'API — contrainte F3 : « appels API dans une
 * couche dédiée, pas de fetch dispersé ».
 *
 * Aucun composant n'appelle `HttpClient` directement, et aucune règle de gestion
 * ne vit ici : ce service traduit des appels HTTP en observables typés, rien de
 * plus. La moyenne affichée, en particulier, vient de `GET /api/tableau` et
 * n'est jamais recalculée côté navigateur (F3).
 */
@Injectable({ providedIn: 'root' })
export class Presence48Api {
  private readonly http = inject(HttpClient);
  private readonly base = '/api';

  // ---- Référentiel ----------------------------------------------------------

  promotions(): Observable<Promotion[]> {
    return this.appel(this.http.get<Promotion[]>(`${this.base}/promotions`));
  }

  etudiants(promotionId: number): Observable<Etudiant[]> {
    return this.appel(
      this.http.get<Etudiant[]>(`${this.base}/promotions/${promotionId}/etudiants`),
    );
  }

  // ---- Sessions -------------------------------------------------------------

  ouvrirSession(titre: string, promotionId: number): Observable<SessionOuverte> {
    return this.appel(
      this.http.post<SessionOuverte>(`${this.base}/sessions`, { titre, promotionId }),
    );
  }

  sessions(promotionId: number): Observable<Session[]> {
    return this.appel(
      this.http.get<Session[]>(`${this.base}/sessions`, { params: { promotionId } }),
    );
  }

  cloturerSession(sessionId: number): Observable<Session> {
    return this.appel(this.http.post<Session>(`${this.base}/sessions/${sessionId}/cloture`, {}));
  }

  // ---- Tableau du formateur -------------------------------------------------

  /**
   * La moyenne renvoyée ici est la seule affichée. F3 interdit de la recalculer
   * dans le navigateur, et `moyenne` peut valoir `null` (RG18).
   */
  tableau(promotionId: number): Observable<LigneTableau[]> {
    return this.appel(
      this.http.get<LigneTableau[]>(`${this.base}/tableau`, { params: { promotionId } }),
    );
  }

  // ---- Présences ------------------------------------------------------------

  marquerPresence(code: string, etudiantId: number): Observable<Presence> {
    return this.appel(this.http.post<Presence>(`${this.base}/presences`, { code, etudiantId }));
  }

  // ---- Exercices ------------------------------------------------------------

  deposerExercice(
    sessionId: number,
    etudiantId: number,
    lien: string,
  ): Observable<ExerciceDepose> {
    return this.appel(
      this.http.post<ExerciceDepose>(`${this.base}/exercices`, { sessionId, etudiantId, lien }),
    );
  }

  // ---- Relectures -----------------------------------------------------------

  relecturesARendre(relecteurId: number): Observable<RelectureATraiter[]> {
    return this.appel(
      this.http.get<RelectureATraiter[]>(`${this.base}/relectures`, {
        params: { relecteurId },
      }),
    );
  }

  ouvrirRelecture(relectureId: number, relecteurId: number): Observable<RelectureATraiter> {
    return this.appel(
      this.http.post<RelectureATraiter>(
        `${this.base}/relectures/${relectureId}/ouverture`,
        {},
        { params: { relecteurId } },
      ),
    );
  }

  rendreRelecture(
    relectureId: number,
    note: number,
    commentaire: string,
    relecteurId: number,
  ): Observable<void> {
    return this.appel(
      this.http.post<void>(`${this.base}/relectures/${relectureId}`, {
        note,
        commentaire,
        relecteurId,
      }),
    );
  }

  // ---- Traduction des erreurs ----------------------------------------------

  /**
   * Transforme toute erreur HTTP en `ErreurApi`.
   *
   * Le backend garantit le format `{ code, message }` pour toutes ses erreurs
   * (ENF4), donc le message affiché à l'utilisateur vient du serveur et n'est
   * pas réécrit ici — sauf quand le serveur est injoignable, où il n'y a
   * justement aucun corps à lire.
   */
  private appel<T>(source: Observable<T>): Observable<T> {
    return source.pipe(
      catchError((erreur: HttpErrorResponse) => {
        const corps = erreur.error as ErreurApi | null;
        if (corps?.code && corps?.message) {
          return throwError(() => corps);
        }
        return throwError(
          () =>
            ({
              code: 'SERVEUR_INJOIGNABLE',
              message:
                "Le serveur ne répond pas. Vérifie que le backend tourne sur le port 8080.",
            }) satisfies ErreurApi,
        );
      }),
    );
  }
}
