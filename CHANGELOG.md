# Journal des modifications — Présence48

Toutes les versions notables du projet. Les identifiants `#n` renvoient aux issues
du dépôt, les `EFx` et `RGx` au cahier des charges.

Format inspiré de [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/).

---

## [v0.1] — 25 septembre 2026

Première version livrable. **Les onze exigences Must**, une branche et une pull
request par issue.

### Backend — les cinq opérations imposées

- `POST /api/sessions` — ouverture d'une session et code de présence (`#1`)
  · expiration à 15 minutes calculée et jamais saisie (RG1)
  · code tiré au hasard cryptographique, alphabet sans caractère ambigu (RG6)
- `POST /api/presences` — marquage de présence (`#3`)
  · `400 CODE_INCONNU`, `410 CODE_EXPIRE`, `409 DEJA_PRESENT`
  · ordre des vérifications conforme au diagramme D3
- `POST /api/exercices` — dépôt du lien d'un exercice (`#4`)
  · dépôt tardif autorisé tant que la session n'est pas clôturée (RG11, Q12)
- `POST /api/relectures/{id}` — rendu définitif d'une note et d'un commentaire (`#7`)
- `GET /api/tableau` — tableau récapitulatif du formateur (`#8`)
  · une seule requête agrégée, pas de N+1 (ENF2)

### Backend — les opérations ajoutées

- `POST /api/sessions/{id}/cloture` — **la clôture, absente de la demande du
  client** alors que Q10, Q12 et Q13 en dépendent (`#9`, RG19)
- `GET /api/promotions` et `GET /api/promotions/{id}/etudiants` — le référentiel
  sans lequel Q1 est inapplicable (`#2`)
- `GET /api/relectures?relecteurId=` et `POST /api/relectures/{id}/ouverture` —
  ce que le relecteur doit relire, et l'instant où sa relecture commence (`#6`,
  RG12)
- `GET /api/sessions?promotionId=` — les sessions d'une promotion (`#1`)

### Backend — socle et transverse

- Squelette Spring Boot 3.5, Java 17, wrapper `mvnw` commité, PostgreSQL
  conteneurisé, schéma versionné par Flyway et jeu de démonstration (`#11`)
- Gestion centralisée des erreurs : tout échec répond `{ code, message }` avec le
  statut exact du contrat, y compris route inconnue, verbe refusé et plantage
  imprévu (`#10`)
- Tirage au sort du relecteur parmi les présents, auteur exclu (`#5`, RG2, RG8)
  · exercice laissé en attente d'assignation si personne n'est éligible (RG20)

### Frontend

- Couche d'appels API dédiée et écran formateur (`#28`)
- Écran étudiant : présence et dépôt d'exercice (`#29`)
- Écran relecteur : ouverture et notation (`#30`)

### Analyse

- Cahier des charges **v1.3** : 16 exigences, 22 règles de gestion, contradiction
  Q10 / Q15 tranchée en faveur de Q15, dix zones d'ombre documentées
- Quatre diagrammes Mermaid, dont le quatrième — états d'un exercice — en bonus
- Contrat d'API complété et **figé avant le premier commit de code**

### Corrigé pendant l'étape 2

- `400 LIEN_INVALIDE` et `400 NOTE_INVALIDE` sortaient en `REQUETE_INVALIDE` :
  RG10 et RG3 déplacées dans les services, qui peuvent nommer le code du contrat
- Jackson acceptait une note de `12.5` et la **tronquait silencieusement en 12** :
  `accept-float-as-int` désactivé (RG3)
- H2 en mémoire survivait d'un contexte Spring à l'autre et les classes de test se
  marchaient sur les pieds : une base par contexte
- `main` cassé quelques minutes par une PR fusionnée sans voir l'échec du build,
  réparé par une PR dédiée

### Connu et assumé

- Reste ouvert : `#12` ajout manuel d'une présence, `#13` remplacement du lien,
  `#14` consultation de sa note — priorité **Should** · `#15` blocage après cinq
  codes erronés — priorité **Could**
- L'ordre de sacrifice était arrêté d'avance en section 10 du cahier des charges
