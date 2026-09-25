# Présence48

Présences, dépôt d'exercices et relecture entre pairs, pour la formation KFOKAM48.

Un formateur ouvre une session de cours et obtient un code ; les étudiants s'en servent pour se
déclarer présents, puis déposent le lien de leur exercice ; un pair tiré au sort le relit et le note ;
le formateur dispose d'un tableau unique — présence, exercices rendus, moyenne reçue, relectures
encore dues.

> **Épreuve finale fullstack KFOKAM48** — Otele Andrea Ines · matricule 237 · centre de Yaoundé
> Analyse et décisions : [`docs/CAHIER_DES_CHARGES.md`](docs/CAHIER_DES_CHARGES.md) ·
> Contrat d'API : [`api/contrat.yaml`](api/contrat.yaml) ·
> Journal : [`docs/JOURNAL.md`](docs/JOURNAL.md)

---

## Prérequis

| Outil | Version attendue |
|---|---|
| Java | 17 ou plus |
| Node | 18 ou plus |
| Docker et Docker Compose | pour la base de données |

Maven n'est pas nécessaire : le wrapper `./mvnw` est versionné dans le dépôt.

## Démarrer — trois commandes

Depuis la racine du dépôt fraîchement cloné, dans trois terminaux :

```bash
# 1. la base de données PostgreSQL
docker compose up -d      # PostgreSQL sur le port 5434

# 2. le backend, sur http://localhost:8080
cd backend && ./mvnw spring-boot:run

# 3. le frontend, sur http://localhost:4200
cd frontend && npm install && npm start
```

Aucune configuration n'est à faire : les valeurs par défaut de `docker-compose.yml` et de
`application.yml` concordent. Pour les changer, copiez `.env.example` en `.env` — ce fichier est
ignoré par Git, aucun secret ne doit être commité.

Au premier démarrage du backend, **Flyway crée le schéma et charge les données de démonstration**.
Vous n'ouvrez donc jamais une application vide.

## Les trois écrans

| Écran | Adresse | Ce qu'on y fait |
|---|---|---|
| Formateur | <http://localhost:4200/formateur> | ouvrir une session et lire son code, clôturer, consulter le tableau |
| Étudiant | <http://localhost:4200/etudiant> | se choisir dans la liste, marquer sa présence, déposer son exercice |
| Relecteur | <http://localhost:4200/relecteur> | voir ses relectures assignées, ouvrir un exercice, rendre une note |

Le frontend appelle l'API via un proxy : `/api` est redirigé vers
`http://localhost:8080`, il n'y a donc aucune adresse à configurer.

## Choix du frontend

**Angular**, parce que son injection de dépendances impose naturellement une couche de services
séparée des composants — la contrainte `F3` du sujet — et que son client HTTP typé rend la
conformité au contrat d'API vérifiable dès la compilation.

## Les données de démonstration

Chargées par la migration `V2__donnees_demo.sql`, elles sont conçues pour que chaque écran montre
quelque chose d'intéressant immédiatement :

| | |
|---|---|
| 2 promotions | « Promotion 2026 — Yaoundé » (12 étudiants) et « — Douala » (8 étudiants) |
| Session `JAVA-COL1` | **clôturée** : plus aucun dépôt ni rendu possible |
| Session `SPRG-LAY2` | **code expiré mais session ouverte** : le dépôt y reste possible |
| Session `ANGU-SRV3` | **ouverte au démarrage** : son code fonctionne pendant 15 minutes |
| Session `GIT-HIST4` | clôturée, sur la promotion de Douala |

Le tableau du formateur de la promotion de Yaoundé montre d'emblée des moyennes nulles chez les
étudiants sans note reçue, des relectures en attente, une présence ajoutée à la main par le
formateur, et un exercice sans relecteur.

> Les codes de présence expirent 15 minutes après l'ouverture de la session. Passé ce délai,
> ouvrez une nouvelle session depuis l'écran formateur pour obtenir un code utilisable — c'est
> le fonctionnement attendu, pas un défaut.

## Les tests

```bash
cd backend && ./mvnw test
```

**103 tests**, dont le test unitaire de la règle RG2 — un étudiant ne relit jamais son propre
exercice, vérifié sur mille tirages au sort — et les tests d'intégration de tous les codes
d'erreur du contrat.

Ils tournent **sans Postgres** : le profil de test rejoue les mêmes migrations Flyway sur H2 en mode
compatibilité PostgreSQL. Vous n'avez donc pas besoin d'avoir lancé `docker compose` pour les
exécuter.

## Structure du dépôt

```
docs/          cahier des charges, journal de bord, diagrammes D1 à D4 (Mermaid)
api/           contrat.yaml — le contrat d'API, figé avant le premier commit de code
backend/       Spring Boot 3.5, Java 17, Maven + wrapper, Flyway
frontend/      Angular
docker-compose.yml
```

## Réinitialiser complètement la base

```bash
docker compose down -v && docker compose up -d
```

Le schéma et les données de démonstration sont recréés à l'identique au prochain démarrage du
backend.
