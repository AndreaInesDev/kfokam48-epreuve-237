# Soumission — Épreuve finale fullstack KFOKAM48

---

## Candidat

| | |
|---|---|
| Nom et prénom(s) | Otele Andrea Ines |
| Matricule | 237 |
| Centre | Yaoundé |
| Compte GitHub | AndreaInesDev |

## Projet

| | |
|---|---|
| Dépôt (public) | `https://github.com/AndreaInesDev/kfokam48-epreuve-237` |
| Commit final — hash complet, 40 caractères | `aa4f458beb4d3b6a15e77ab6d73bdef58d07bb7c` |
| Branche | `main` |

## Technique

| | |
|---|---|
| Frontend utilisé | Angular 22 |
| Base de données | PostgreSQL 16, conteneurisée · schéma versionné par Flyway |
| Commandes de démarrage | `docker compose up -d` · `cd backend && ./mvnw spring-boot:run` · `cd frontend && npm install && npm start` |

> PostgreSQL est exposé sur le port **5434** et non 5432, pour ne pas entrer en
> conflit avec un PostgreSQL déjà installé sur le poste du correcteur. Aucune
> configuration n'est à faire : les valeurs par défaut de `docker-compose.yml` et
> d'`application.yml` concordent.

## Ce que j'ai livré

**Ce qui fonctionne.** Les cinq opérations imposées du contrat, plus neuf opérations
ajoutées dont la clôture de session — le trou de la demande du client, dont Q10, Q12
et Q13 dépendaient toutes. Les trois écrans exigés par `F2`. Schéma versionné par
Flyway avec un jeu de démonstration conçu pour que le tableau du formateur montre
d'emblée les cas intéressants : moyennes nulles, relectures en attente, présence
ajoutée à la main, exercice sans relecteur. **103 tests** passent sans PostgreSQL
installé, dont le test unitaire de RG2 sur mille tirages au sort et les tests
d'intégration de tous les codes d'erreur du contrat. Vérifié de bout en bout contre
le vrai PostgreSQL avant de soumettre.

**Ce qui ne fonctionne pas.** Rien de ce qui est livré n'est en échec connu.

**Ce que j'ai volontairement laissé de côté, et pourquoi.** Quatre issues restent
ouvertes, priorisées et commentées une par une sur le dépôt : `#12` ajout manuel
d'une présence, `#13` remplacement du lien d'un exercice, `#14` consultation de sa
note par l'étudiant — priorité **Should** — et `#15` blocage après cinq codes
erronés — priorité **Could**. L'ordre de sacrifice était arrêté d'avance en section
10 du cahier des charges, et il a été suivi sans être réécrit après coup. `#15` en
particulier a été écartée pour deux raisons écrites dès l'étape 1 : c'est une
protection de confort et non de sécurité, puisque sans authentification (Q1) elle se
contourne en changeant d'identifiant, et elle exigerait une révision du diagramme D2.

**Une erreur que j'assume.** J'ai fusionné une pull request sans voir que le build
échouait, et `main` est resté cassé quelques minutes. Réparé par une pull request
dédiée qui dit ce qui s'est passé, et consigné dans le journal plutôt que masqué.

---

## Avant de téléverser, vérifie

- [ ] Mon dépôt est **public** et s'ouvre en navigation privée
- [ ] Mon hash fait bien **40 caractères** et existe sur GitHub
- [ ] Tout mon travail est **poussé** — `git status` est propre
- [ ] Mon `README` a été testé depuis un clone vierge, dans un dossier vide
- [ ] Mon `JOURNAL.md` et mon cahier des charges sont dans `docs/`
- [ ] Les trois commits `[JALON]` sont poussés et dans le bon ordre

---

**Déclaration.** J'ai réalisé ce travail seule. Les outils d'IA étaient autorisés
sans restriction et je les ai utilisés ; mon journal indique où et comment j'ai
vérifié leurs réponses. Mon dépôt restera public et inchangé jusqu'à la publication
des résultats.

Signature : ______________________  Date : 25 septembre 2026
