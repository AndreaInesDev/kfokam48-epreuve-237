# Journal de bord — Otele Andrea Ines · matricule 237

> Une entrée **par étape**, écrite au moment où elle se termine.
> **Fait** — ce qui vient d'être terminé · **Bloqué** — ce qui a coûté du temps, et combien ·
> **IA** — ce qui lui a été demandé, et comment sa réponse a été vérifiée.

---

## Étape 1 — Analyse et conception

**Fait :** cahier des charges complet (16 exigences fonctionnelles, 22 règles de gestion, 8
exigences non fonctionnelles, les dix sections imposées) ; les quatre diagrammes en Mermaid, dont
le quatrième — états-transitions de l'exercice — pour le bonus ; contrat d'API complété de 9
opérations et figé ; 15 issues créées avec critères d'acceptation, étiquettes Must/Should/Could et
répartition en jalons GitHub `v0.1` et `v1.0` ; commit `[JALON] analyse` poussé. Aucune ligne de
code, pas de `spring init`.

**Bloqué :** environ 25 minutes au total, sur trois points.

*Le plus long* — la contradiction `Q10` / `Q15`. Le modèle de journal fourni avec le sujet tranche,
en exemple, en faveur de `Q10`. J'ai tranché dans l'autre sens, pour `Q15`, parce que le contrat
d'API impose `409 RELECTURE_DEJA_RENDUE` : choisir `Q10` obligerait à ne jamais renvoyer ce code et
violerait la contrainte `B2`. Un document contractuel me paraît primer sur une réponse orale. Écrit
et argumenté en section 7 plutôt que subi.

*Deux zones d'ombre découvertes en cours de route*, pas à la lecture. En traçant le diagramme `D4`,
je me suis aperçu que `Q13` autorise à remplacer son lien « tant que personne n'a commencé à le
relire », alors que le relecteur est assigné dès le dépôt : si « commencer à relire » voulait dire
« être assigné », un remplacement n'aurait jamais été possible. J'ai posé que la relecture commence
à la première ouverture par le relecteur (`consultee_at`), et le cahier des charges est passé en
version 1.1. Puis en rédigeant le contrat, une hypothèse restée implicite est apparue : faut-il être
présent pour déposer un exercice ? Tranchée oui, `RG23`, version 1.2. Les deux révisions sont
datées dans le journal des révisions du cahier des charges — l'analyse a bougé pendant l'étape 1, et
l'historique Git le montre.

*Une interruption matérielle* : coupure réseau de quelques minutes pendant l'installation de l'outil
`gh`, sans conséquence sur le travail.

**IA :** utilisée sans restriction, pour rédiger le cahier des charges, les diagrammes, le contrat
et les issues. Ce que j'ai vérifié, et comment :

- **Le contrat imposé n'a-t-il pas été altéré ?** C'est la contrainte `B2` et elle ne pardonne pas.
  Vérifié par un script qui compare, opération par opération, le contrat d'origine et le mien :
  corps de requête, paramètres et chacune des réponses imposées. Résultat : les cinq opérations sont
  intactes, et les seuls écarts sont des réponses **ajoutées**, jamais modifiées ni retirées.
- **Les diagrammes se rendent-ils vraiment ?** Un diagramme Mermaid cassé s'affiche en bloc d'erreur
  sur GitHub, et 12 points en dépendent. Les quatre ont été validés avant d'être commités. J'ai
  aussi retiré les balises `<b>` et les emoji des libellés, que le rendu Mermaid de GitHub ne
  garantit pas.
- **Les codes d'erreur sont-ils cohérents entre les documents ?** Script de comparaison entre les
  codes cités dans le cahier des charges et l'énuméré `CodeErreur` du contrat. Aucun code cité dans
  le cahier des charges ne manque au contrat.
- **Les issues sont-elles des résultats ou des tâches techniques ?** Test de relecture, titre par
  titre : est-ce que le client le comprendrait ? Deux issues sur quinze ne viennent pas d'une
  exigence fonctionnelle mais d'exigences non fonctionnelles (`#10` format d'erreur, `#11` démarrage
  chez un tiers) ; je les ai reformulées en résultats, avec le correcteur pour bénéficiaire, plutôt
  que de les intituler « configurer Flyway » ou « ajouter un `@RestControllerAdvice` ».
- **Sur un point, j'ai écarté la recommandation de l'IA.** Elle proposait de réécrire le message du
  commit racine pour le renommer, puis a changé d'avis quand le sujet mis à jour a ajouté une
  interdiction explicite. J'ai choisi de ne pas y toucher : un `push --force` sur le `main` du projet
  coûte −5 au barème, et le gain était cosmétique. Voir la note ci-dessous.

**Note sur le commit racine.** Le premier commit du dépôt, `a0f111f`, porte le message
`[JALON] depart`. C'était le message imposé par la première version du `LISEZ-MOI` pour le test de
connexion ; la version mise à jour l'a renommé `chore: verification du depot` et interdit d'employer
le préfixe `[JALON]` pour autre chose. **Ce commit n'est pas un jalon.** Les trois jalons du sujet
sont `[JALON] analyse`, `[JALON] v0.1` et `[JALON] v1.0`. J'ai préféré le laisser tel quel et
l'expliquer ici plutôt que de réécrire l'historique de `main`.

---

## Étape 2 — Première version

**Fait :** les onze exigences **Must** livrées, une branche et une pull request par
issue, chaque issue fermée par son commit. Backend Spring Boot complet : les cinq
opérations imposées du contrat plus neuf opérations ajoutées, schéma versionné par
Flyway avec un jeu de démonstration, **103 tests** dont le test unitaire de RG2 et
le test d'intégration des codes d'erreur exigés par `B6`. Frontend Angular : les
trois écrans de `F2`, une couche d'appels API dédiée, états de chargement et
d'erreur. Trois issues ouvertes en cours de route (`#28`, `#29`, `#30`) pour rendre
le frontend traçable : le backlog initial mélangeait API et interface dans les
mêmes exigences, et le code des écrans ne se rattachait à aucun ticket.

**Bloqué :** trois choses, environ 40 minutes au total.

*Deux défauts de conformité au contrat, trouvés par les tests et non par relecture.*
`400 LIEN_INVALIDE` et `400 NOTE_INVALIDE` sont exigés par le contrat, mais une
violation de contrainte déclarative Jakarta ressort en `REQUETE_INVALIDE` : RG10 et
RG3 sont donc vérifiées dans les services, qui peuvent nommer le code exact. Et
Jackson acceptait une note de `12.5` en la tronquant silencieusement en 12 —
`accept-float-as-int` désactivé, un arrondi silencieux sur une note serait pire
qu'un refus.

*Un défaut d'isolation des tests.* H2 en mémoire nommé `presence48` survivait d'un
contexte Spring à l'autre dans la même JVM : les classes de test se marchaient sur
les pieds. Révélé par un test du tableau qui lisait des données déposées par une
autre classe. Corrigé à la racine, une base par contexte.

*Une erreur de ma part.* J'ai fusionné la PR `#26` sans voir que le build échouait,
et `main` est resté cassé quelques minutes. Réparé par une PR dédiée qui dit ce qui
s'est passé. Les tests de clôture dépendaient de leur ordre d'exécution — le défaut
était dans les tests, pas dans la règle.

**IA :** utilisée pour écrire l'essentiel du code. Ce que j'ai vérifié, et comment :

- **La conformité au contrat, par un test qui lit le contrat.** `CodeErreurContratTest`
  ouvre `api/contrat.yaml` et vérifie que l'énumération Java en est le miroir exact,
  code par code et statut par statut, dans les deux sens. Une dérive fait échouer le
  build, pas une relecture humaine.
- **Les réponses de l'API, champ par champ.** Pour chaque opération imposée, un test
  vérifie que le corps de succès contient *exactement* les champs du contrat, ni plus
  ni moins. C'est ce qui a montré que l'IA ajoutait volontiers des champs « utiles »
  hors contrat.
- **Les règles de gestion, pas le code qui les porte.** RG2 est testée sur mille
  tirages au sort, RG1 en mesurant l'écart entre ouverture et expiration, RG18 en
  exigeant `null` et non zéro. Une règle qu'on ne peut pas faire échouer n'est pas
  testée.
- **Le jeu de démonstration est testé comme du code.** Sept assertions vérifient
  qu'il respecte RG1, RG2, RG8, RG15, RG18 et RG20 : des données de démonstration qui
  violeraient les règles seraient pires que pas de données.
- **Ce que j'ai refusé.** L'IA proposait d'ajouter `relecteurId` en champ obligatoire
  du corps de `POST /api/relectures/{id}`, ce qui aurait modifié une opération
  imposée. Retenu en champ *facultatif* : le corps du contrat reste valable tel quel.

---

## Étape 3 — Enveloppe

**Fait :**

**Bloqué :**

**IA :**

**Ce que j'ai sorti du périmètre pour absorber le changement, et pourquoi :**

---

## Étape 4 — Version finale

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 5 — Soumission

**Fait :**

**Ce que je referais autrement avec une journée de plus :**
