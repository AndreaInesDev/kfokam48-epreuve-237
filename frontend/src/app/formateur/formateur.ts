import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Presence48Api } from '../api/presence48-api';
import { ErreurApi, LigneTableau, Promotion, Session, SessionOuverte } from '../api/modeles';

/**
 * Écran formateur — F2 : ouvrir une session, voir le tableau, clôturer.
 *
 * Aucune règle de gestion ici : la moyenne affichée est celle que renvoie
 * `GET /api/tableau`, jamais recalculée (F3). Le composant ne fait que demander,
 * afficher, et montrer l'erreur que le serveur a nommée.
 */
@Component({
  selector: 'app-formateur',
  imports: [FormsModule],
  templateUrl: './formateur.html',
})
export class Formateur {
  private readonly api = inject(Presence48Api);

  protected readonly promotions = signal<Promotion[]>([]);
  protected readonly promotionId = signal<number | null>(null);
  protected readonly titre = signal('');

  protected readonly sessions = signal<Session[]>([]);
  protected readonly tableau = signal<LigneTableau[]>([]);
  protected readonly derniereSession = signal<SessionOuverte | null>(null);

  protected readonly chargement = signal(false);
  protected readonly erreur = signal<ErreurApi | null>(null);

  constructor() {
    this.chargement.set(true);
    this.api.promotions().subscribe({
      next: (promotions) => {
        this.promotions.set(promotions);
        if (promotions.length > 0) {
          this.choisirPromotion(promotions[0].id);
        }
        this.chargement.set(false);
      },
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  protected choisirPromotion(id: number): void {
    this.promotionId.set(id);
    this.rafraichir();
  }

  protected rafraichir(): void {
    const promotionId = this.promotionId();
    if (promotionId === null) {
      return;
    }
    this.chargement.set(true);
    this.erreur.set(null);
    this.api.sessions(promotionId).subscribe({
      next: (sessions) => this.sessions.set(sessions),
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
    this.api.tableau(promotionId).subscribe({
      next: (lignes) => {
        this.tableau.set(lignes);
        this.chargement.set(false);
      },
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  protected ouvrirSession(): void {
    const promotionId = this.promotionId();
    if (promotionId === null || this.titre().trim() === '') {
      return;
    }
    this.chargement.set(true);
    this.erreur.set(null);
    this.api.ouvrirSession(this.titre().trim(), promotionId).subscribe({
      next: (session) => {
        this.derniereSession.set(session);
        this.titre.set('');
        this.chargement.set(false);
        this.rafraichir();
      },
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  protected cloturer(sessionId: number): void {
    this.chargement.set(true);
    this.erreur.set(null);
    this.api.cloturerSession(sessionId).subscribe({
      next: () => {
        this.chargement.set(false);
        this.rafraichir();
      },
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  /** RG18 : une moyenne absente s'affiche en tiret, jamais en zéro. */
  protected moyenneAffichee(ligne: LigneTableau): string {
    return ligne.moyenne === null ? '—' : ligne.moyenne.toFixed(2);
  }

  protected heure(instant: string): string {
    return new Date(instant).toLocaleString('fr-FR');
  }

  private echouer(erreur: ErreurApi): void {
    this.erreur.set(erreur);
    this.chargement.set(false);
  }
}
