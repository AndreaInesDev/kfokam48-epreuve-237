# Journal de bord — matricule 237

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

**Fait :**

**Bloqué :**

**IA :**

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
