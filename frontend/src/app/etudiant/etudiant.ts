import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Presence48Api } from '../api/presence48-api';
import { ErreurApi, Etudiant as EtudiantModele, Promotion, Session } from '../api/modeles';

/**
 * Écran étudiant — F2 : marquer sa présence, déposer son exercice.
 *
 * Q1 : aucun mot de passe, l'étudiant se choisit dans la liste de sa promotion.
 * Les messages d'échec affichés sont ceux du serveur : c'est lui qui distingue
 * un code inconnu (400) d'un code expiré (410) et d'une présence déjà
 * enregistrée (409), et le front n'a pas à rejouer cette logique.
 */
@Component({
  selector: 'app-etudiant',
  imports: [FormsModule],
  templateUrl: './etudiant.html',
})
export class Etudiant {
  private readonly api = inject(Presence48Api);

  protected readonly promotions = signal<Promotion[]>([]);
  protected readonly etudiants = signal<EtudiantModele[]>([]);
  protected readonly sessions = signal<Session[]>([]);

  protected readonly promotionId = signal<number | null>(null);
  protected readonly etudiantId = signal<number | null>(null);

  protected readonly code = signal('');
  protected readonly sessionId = signal<number | null>(null);
  protected readonly lien = signal('');

  protected readonly chargement = signal(false);
  protected readonly erreur = signal<ErreurApi | null>(null);
  protected readonly succes = signal<string | null>(null);

  constructor() {
    this.chargement.set(true);
    this.api.promotions().subscribe({
      next: (promotions) => {
        this.promotions.set(promotions);
        this.chargement.set(false);
        if (promotions.length > 0) {
          this.choisirPromotion(promotions[0].id);
        }
      },
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  protected choisirPromotion(id: number): void {
    this.promotionId.set(id);
    this.etudiantId.set(null);
    this.reinitialiserMessages();
    this.api.etudiants(id).subscribe({
      next: (etudiants) => this.etudiants.set(etudiants),
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
    // Les sessions non clôturées sont celles où un dépôt reste possible (RG11).
    this.api.sessions(id).subscribe({
      next: (sessions) => this.sessions.set(sessions.filter((session) => !session.cloturee)),
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  protected marquerPresence(): void {
    const etudiantId = this.etudiantId();
    if (etudiantId === null || this.code().trim() === '') {
      return;
    }
    this.reinitialiserMessages();
    this.chargement.set(true);
    this.api.marquerPresence(this.code().trim().toUpperCase(), etudiantId).subscribe({
      next: (presence) => {
        this.succes.set(
          `Présence enregistrée pour la session ${presence.sessionId} (source ${presence.source}).`,
        );
        this.code.set('');
        this.chargement.set(false);
        this.choisirPromotion(this.promotionId()!);
      },
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  protected deposerExercice(): void {
    const etudiantId = this.etudiantId();
    const sessionId = this.sessionId();
    if (etudiantId === null || sessionId === null || this.lien().trim() === '') {
      return;
    }
    this.reinitialiserMessages();
    this.chargement.set(true);
    this.api.deposerExercice(sessionId, etudiantId, this.lien().trim()).subscribe({
      next: (exercice) => {
        this.succes.set(`Exercice déposé (statut ${exercice.statut}).`);
        this.lien.set('');
        this.chargement.set(false);
      },
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  private reinitialiserMessages(): void {
    this.erreur.set(null);
    this.succes.set(null);
  }

  private echouer(erreur: ErreurApi): void {
    this.erreur.set(erreur);
    this.chargement.set(false);
  }
}
