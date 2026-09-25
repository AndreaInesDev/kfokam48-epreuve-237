import { Routes } from '@angular/router';

/**
 * Les trois écrans exigés par la contrainte F2, un par acteur.
 *
 * Chargés à la demande : le frontend n'a pas besoin de charger l'écran formateur
 * pour qu'un étudiant marque sa présence sur son téléphone (ENF1).
 */
export const routes: Routes = [
  { path: '', redirectTo: 'formateur', pathMatch: 'full' },
  {
    path: 'formateur',
    title: 'Formateur — Présence48',
    loadComponent: () => import('./formateur/formateur').then((m) => m.Formateur),
  },
  {
    path: 'etudiant',
    title: 'Étudiant — Présence48',
    loadComponent: () => import('./etudiant/etudiant').then((m) => m.Etudiant),
  },
  // La route /relecteur arrive avec l'issue #30.
  { path: '**', redirectTo: 'formateur' },
];
