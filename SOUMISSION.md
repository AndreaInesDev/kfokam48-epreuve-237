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
| Commit final — hash complet, 40 caractères | `ea290e3352cf7498fcbe8db97d6a8eeb53d95769` |
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

**L'étape 3.** Enveloppe obtenue à 17h37. Le bug signalé par le client a été diagnostiqué,
reproduit par un test qui échoue, puis corrigé — issue `#34` ouverte **avant** le premier
commit de code, correctif dans sa propre branche et sa propre pull request. Ce n'était pas
la contrainte d'unicité qu'on soupçonnerait, mais une régression que j'avais introduite
moi-même : le retirage au sort de RG20 annulait une présence valide par ricochet
transactionnel. Le changement de besoin — deux relecteurs par exercice, moyenne des deux,
provisoire si une seule est rendue — a été traité dans une **seconde** branche et une
seconde pull request : migration `V3` ajoutée sans jamais modifier les précédentes, cahier
des charges en v1.4, diagrammes D2 et D4 corrigés, contrat en v1.2.

**Ce qui ne fonctionne pas.** Rien de ce qui est livré n'est en échec connu.
L'implémentation du double tirage et du calcul de moyenne provisoire n'est pas codée :
elle reste en `#36` et `#37`, et le choix est justifié en section 10 du cahier des charges.
J'ai livré ce qui fige la décision et ce qui est irréversible en base — l'analyse, la
migration, le contrat — plutôt qu'un service qui contredirait le schéma.

**Ce que j'ai volontairement laissé de côté, et pourquoi.** Quatre issues restent
ouvertes avant l'étape 3, priorisées et commentées une par une sur le dépôt : `#12` ajout manuel
d'une présence, `#13` remplacement du lien d'un exercice, `#14` consultation de sa
note par l'étudiant — priorité **Should** — et `#15` blocage après cinq codes
erronés — priorité **Could**. L'ordre de sacrifice était arrêté d'avance en section
10 du cahier des charges, et il a été suivi sans être réécrit après coup. `#15` en
particulier a été écartée pour deux raisons écrites dès l'étape 1 : c'est une
protection de confort et non de sécurité, puisque sans authentification (Q1) elle se
contourne en changeant d'identifiant, et elle exigerait une révision du diagramme D2.
Les issues `#36` et `#37`, nées du changement de l'étape 3, passent **devant** elles :
un `Must` du client vaut plus qu'un `Should` que je m'étais donné.

**Sur l'ordre des jalons.** `[JALON] v1.0` a été posé à 17h31, avant que l'enveloppe ne me
parvienne, pour sécuriser une soumission complète avant la fermeture de la plateforme. Le
travail de l'étape 3 apparaît donc après ce jalon dans l'historique. C'est un compromis
assumé face à l'échéance, consigné dans le journal : je préférais une soumission valide
sans l'étape 3 à une étape 3 parfaite non soumise.

**Une erreur que j'assume.** J'ai fusionné une pull request sans voir que le build
échouait, et `main` est resté cassé quelques minutes. Réparé par une pull request
dédiée qui dit ce qui s'est passé, et consigné dans le journal plutôt que masqué.

---

## Avant de téléverser, vérifie

- [x] Mon dépôt est **public** et s'ouvre en navigation privée
- [x] Mon hash fait bien **40 caractères** et existe sur GitHub
- [x] Tout mon travail est **poussé** — `git status` est propre
- [x] Mon `README` a été testé depuis un clone vierge, dans un dossier vide
- [x] Mon `JOURNAL.md` et mon cahier des charges sont dans `docs/`
- [x] Les trois commits `[JALON]` sont poussés et dans le bon ordre

---

**Déclaration.** J'ai réalisé ce travail seule. Les outils d'IA étaient autorisés
sans restriction et je les ai utilisés ; mon journal indique où et comment j'ai
vérifié leurs réponses. Mon dépôt restera public et inchangé jusqu'à la publication
des résultats.

Signature : ______________________  Date : 25 septembre 2026
