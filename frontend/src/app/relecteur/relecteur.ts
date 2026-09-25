import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Presence48Api } from '../api/presence48-api';
import { ErreurApi, Etudiant, Promotion, RelectureATraiter } from '../api/modeles';

/**
 * Écran relecteur — F2 : faire une relecture.
 *
 * Le relecteur ne choisit pas ce qu'il relit : c'est le système qui le lui
 * assigne au hasard (RG8). Cet écran ne fait donc que lire la liste assignée.
 *
 * Il n'affiche jamais l'identité de l'auteur, et ce n'est pas un choix
 * d'affichage : le DTO du serveur n'en contient aucune (RG14, Q8).
 */
@Component({
  selector: 'app-relecteur',
  imports: [FormsModule],
  templateUrl: './relecteur.html',
})
export class Relecteur {
  private readonly api = inject(Presence48Api);

  protected readonly promotions = signal<Promotion[]>([]);
  protected readonly etudiants = signal<Etudiant[]>([]);
  protected readonly relecteurId = signal<number | null>(null);
  protected readonly aRendre = signal<RelectureATraiter[]>([]);

  /** La relecture en cours de saisie, ouverte donc figée pour l'auteur (RG12). */
  protected readonly enCours = signal<RelectureATraiter | null>(null);
  protected readonly note = signal<number | null>(null);
  protected readonly commentaire = signal('');

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
    this.reinitialiser();
    this.api.etudiants(id).subscribe({
      next: (etudiants) => this.etudiants.set(etudiants),
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  protected choisirRelecteur(id: number | null): void {
    this.relecteurId.set(id);
    this.enCours.set(null);
    this.aRendre.set([]);
    if (id === null) {
      return;
    }
    this.chargement.set(true);
    this.api.relecturesARendre(id).subscribe({
      next: (relectures) => {
        this.aRendre.set(relectures);
        this.chargement.set(false);
      },
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  /**
   * RG12 — ouvrir l'exercice marque le début de la relecture, et à partir de cet
   * instant l'auteur ne peut plus remplacer son lien. C'est pour cela que
   * l'ouverture est un geste explicite du relecteur et non un effet du chargement
   * de la liste.
   */
  protected ouvrir(relecture: RelectureATraiter): void {
    const relecteurId = this.relecteurId();
    if (relecteurId === null) {
      return;
    }
    this.reinitialiserMessages();
    this.chargement.set(true);
    this.api.ouvrirRelecture(relecture.id, relecteurId).subscribe({
      next: (ouverte) => {
        this.enCours.set(ouverte);
        this.note.set(null);
        this.commentaire.set('');
        this.chargement.set(false);
      },
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  protected rendre(): void {
    const relecture = this.enCours();
    const relecteurId = this.relecteurId();
    const note = this.note();
    if (relecture === null || relecteurId === null || note === null) {
      return;
    }
    this.reinitialiserMessages();
    this.chargement.set(true);
    this.api.rendreRelecture(relecture.id, note, this.commentaire(), relecteurId).subscribe({
      next: () => {
        this.succes.set('Relecture rendue. Elle est définitive.');
        this.enCours.set(null);
        this.chargement.set(false);
        this.choisirRelecteur(relecteurId);
      },
      error: (erreur: ErreurApi) => this.echouer(erreur),
    });
  }

  private reinitialiser(): void {
    this.relecteurId.set(null);
    this.etudiants.set([]);
    this.aRendre.set([]);
    this.enCours.set(null);
    this.reinitialiserMessages();
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
