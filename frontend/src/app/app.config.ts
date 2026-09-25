import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // Toutes les requêtes passent par Presence48Api (F3) ; ce client HTTP est
    // la seule dépendance réseau de l'application.
    provideHttpClient(withFetch()),
  ],
};
