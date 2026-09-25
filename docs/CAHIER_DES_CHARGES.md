# Cahier des charges — Présence48

**Auteur :** Otele Andrea Ines · **Matricule :** 237 · **Centre :** Yaoundé
**Version :** 1.2 · **Date :** 25 septembre 2026
**Frontend choisi :** Angular, parce que son injection de dépendances impose naturellement une couche de services séparée des composants — exactement la contrainte F3 — et que son client HTTP typé rend la conformité au contrat d'API vérifiable à la compilation.

---

## 1. Contexte et objectif

La direction de KFOKAM48 anime des sessions de cours pour des promotions d'une soixantaine
d'étudiants. Aujourd'hui, trois informations vivent dans trois endroits séparés et ne se
recoupent jamais : qui était présent, qui a rendu son exercice, et ce que vaut le travail rendu.
L'appel se fait sur papier ou de mémoire, les exercices arrivent par messagerie, et la relecture
entre pairs — quand elle a lieu — ne laisse aucune trace exploitable.

Présence48 réunit ces trois informations en un seul endroit. Le formateur ouvre une session et
obtient un code éphémère ; les étudiants s'en servent pour se déclarer présents ; ils déposent
ensuite le lien de leur exercice, qu'un pair tiré au sort relit et note. Le formateur dispose
enfin d'un tableau unique qui lui dit, pour chaque étudiant de la promotion, sa présence, ce
qu'il a rendu, la moyenne des notes qu'il a reçues et les relectures qu'il doit encore à ses
camarades.

L'objectif n'est pas de noter les étudiants à la place du formateur, mais de **rendre visible en
un coup d'œil ce qui est aujourd'hui invisible** : l'assiduité réelle, le travail effectivement
rendu, et les relectures en souffrance.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| **Formateur** | Ouvrir une session et obtenir son code · Ajouter une présence à la main (RG15) · Clôturer une session (RG19) · Consulter le tableau récapitulatif de la promotion | Marquer une présence à la place d'un étudiant sans que la source soit tracée · Noter un exercice · Modifier une relecture rendue |
| **Étudiant** | Se choisir dans la liste de sa promotion (RG21) · Marquer sa présence avec le code · Déposer le lien de son exercice · Remplacer ce lien tant que la relecture n'a pas commencé (RG12) · Consulter la note et le commentaire reçus | Marquer sa présence avec un code expiré ou après la clôture · Déposer deux exercices pour la même session · Connaître l'identité de son relecteur (RG14) |
| **Relecteur** | Voir la liste des exercices qui lui sont assignés · Rendre une note entière de 0 à 20 et un commentaire | Relire son propre exercice (RG2) · Revenir sur une relecture validée (RG13) · Choisir l'exercice qu'il relit — c'est le système qui tire au sort (RG8) |
| **Système** | Tirer au sort le relecteur parmi les étudiants présents (RG8) · Faire expirer les codes (RG1) · Calculer les moyennes (RG18) | — |

### Le relecteur est-il un acteur distinct ?

**Non. C'est un étudiant dans un certain état** : celui d'avoir été tiré au sort pour l'exercice
d'un pair. Un même étudiant est simultanément auteur d'un exercice et relecteur de celui d'un
autre, au cours de la même session.

**Conséquence sur le modèle de données, assumée ici et reprise en D2 :** il n'existe **aucune
table `Relecteur`**. Le rôle est porté par une clé étrangère `relecteur_id` sur la table
`relecture`, qui pointe vers `etudiant`. Créer une entité `Relecteur` distincte dupliquerait
l'identité de l'étudiant et rendrait impossible la règle RG2, qui suppose justement de pouvoir
comparer l'auteur et le relecteur comme deux références au même référentiel.

Le « Système » n'est pas un acteur humain : il figure ici parce que le tirage au sort (Q7) et
l'expiration (Q2) sont des comportements que personne ne déclenche, et qu'ils doivent apparaître
dans le diagramme de cas d'utilisation D1 pour que le modèle soit honnête.

## 3. Périmètre

**Inclus dans cette version :**

- Ouverture d'une session de cours par le formateur, avec génération d'un code de présence éphémère
- Marquage de présence par l'étudiant au moyen de ce code, avec les trois cas d'erreur du contrat
- Ajout manuel d'une présence par le formateur, tracé comme tel
- Clôture d'une session par le formateur
- Dépôt et remplacement du lien d'un exercice
- Assignation automatique et aléatoire d'un relecteur parmi les étudiants présents
- Rendu d'une relecture : note entière sur 20 et commentaire
- Consultation par l'étudiant de la note et du commentaire reçus, sans l'identité du relecteur
- Tableau récapitulatif du formateur par promotion
- Référentiel de promotions et d'étudiants en lecture seule, alimenté par un jeu de démonstration
- Trois écrans : formateur, étudiant, relecteur

**Explicitement exclu :**

| Exclu | Pourquoi |
|---|---|
| Authentification, mot de passe, session utilisateur | Q1 : le client refuse explicitement. L'étudiant se désigne dans une liste, sans preuve d'identité |
| Création et administration des promotions et des étudiants | Aucune des 16 réponses ne l'évoque. Le référentiel est chargé par migration de démonstration |
| Plusieurs relecteurs par exercice | Q6 : un seul, sans ambiguïté |
| Réassignation manuelle d'un relecteur par le formateur | Non demandé. Le formateur constate les relectures en attente (Q11), il ne les redistribue pas |
| Notifications par courriel ou SMS | Jamais évoqué, et suppose un référentiel de contacts qui n'existe pas |
| Export du tableau, statistiques historiques, graphiques | Q16 décrit un tableau à l'écran, rien de plus |
| Dépôt de fichiers | Q13 et le contrat ne parlent que d'un **lien** |
| Travail soigné sur l'apparence | Le sujet l'exclut du barème sans détour |
| Traçabilité complète des modifications (audit log) | Seule la source d'une présence est tracée, parce que Q14 l'exige nommément |

Ce que nous excluons compte autant que ce que nous incluons : chaque ligne du tableau ci-dessus
est une fonctionnalité qu'un correcteur pourrait attendre et qu'il ne trouvera pas — il saura
maintenant que c'est un choix, pas un oubli.

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| **EF1** | Le formateur ouvre une session de cours et obtient un code de présence | Quand j'envoie un titre et une promotion existante, alors je reçois `201` avec un `id`, un `code`, une date d'ouverture et une date d'expiration fixée 15 minutes plus tard | Must |
| **EF2** | L'étudiant se choisit dans la liste des étudiants de sa promotion | Quand je demande les étudiants d'une promotion existante, alors je reçois la liste de leurs identifiants et de leurs noms ; si la promotion n'existe pas, je reçois `404 PROMOTION_INCONNUE` | Must |
| **EF3** | L'étudiant marque sa présence à l'aide d'un code | Quand je saisis un code valide et non expiré, alors je reçois `201` et ma présence apparaît dans le tableau du formateur avec la source `ETUDIANT` | Must |
| **EF4** | Le marquage de présence refuse les cas invalides | Quand le code est inconnu je reçois `400 CODE_INCONNU` ; quand il a plus de 15 minutes je reçois `410 CODE_EXPIRE` ; quand je suis déjà présent je reçois `409 DEJA_PRESENT` | Must |
| **EF5** | L'étudiant dépose le lien de son exercice pour une session | Quand j'envoie un lien `http(s)` valide pour une session non clôturée où je suis présent, alors je reçois `201` avec le statut de l'exercice | Must |
| **EF6** | Le dépôt d'exercice refuse les cas invalides | Quand le lien n'est pas une URL `http(s)` je reçois `400 LIEN_INVALIDE` ; quand j'ai déjà déposé pour cette session je reçois `409 EXERCICE_DEJA_DEPOSE` ; quand je n'étais pas présent à la session je reçois `409 ETUDIANT_NON_PRESENT` ; quand la session est clôturée je reçois `409 SESSION_CLOTUREE` | Must |
| **EF7** | Le système assigne automatiquement un relecteur à chaque exercice déposé | Quand un exercice est déposé et qu'au moins un autre étudiant est présent à la session, alors une relecture est créée au nom d'un de ces étudiants tiré au sort, jamais l'auteur | Must |
| **EF8** | Le relecteur consulte les exercices qu'il doit relire | Quand je demande mes relectures, alors je reçois celles qui me sont assignées et ne sont pas encore rendues, avec le lien de l'exercice à relire ; l'ouverture d'une relecture marque le moment où elle commence (RG12) | Must |
| **EF9** | Le relecteur rend une note entière sur 20 et un commentaire | Quand j'envoie une note entière entre 0 et 20 et un commentaire pour une relecture qui m'est assignée et non rendue, alors je reçois `200` et la relecture passe au statut rendu | Must |
| **EF10** | Le rendu d'une relecture refuse les cas invalides | Quand la note est hors 0–20 ou non entière je reçois `400 NOTE_INVALIDE` ; quand l'exercice est le mien je reçois `403 AUTO_RELECTURE` ; quand la relecture est déjà rendue je reçois `409 RELECTURE_DEJA_RENDUE` | Must |
| **EF11** | Le formateur consulte le tableau récapitulatif de sa promotion | Quand je demande le tableau d'une promotion existante, alors je reçois pour chaque étudiant son nombre de présences, son nombre d'exercices déposés, la moyenne des notes reçues et le nombre de relectures qu'il doit encore rendre | Must |
| **EF12** | Le formateur clôture une session | Quand je clôture une session ouverte, alors plus aucun dépôt ni aucune relecture n'y est accepté ; une session déjà clôturée renvoie `409 SESSION_DEJA_CLOTUREE` | Must |
| **EF13** | Le formateur ajoute une présence à la main | Quand j'ajoute la présence d'un étudiant à une session, alors elle apparaît dans le tableau avec la source `FORMATEUR` et se distingue visuellement d'une présence saisie par l'étudiant | Should |
| **EF14** | L'étudiant remplace le lien de son exercice | Quand je renvoie un lien pour un exercice dont la relecture n'a pas commencé, alors le lien est remplacé ; si la relecture a commencé je reçois `409 RELECTURE_COMMENCEE` | Should |
| **EF15** | L'étudiant consulte la note et le commentaire reçus | Quand la relecture de mon exercice est rendue, alors je vois la note et le commentaire, et à aucun moment le nom du relecteur | Should |
| **EF16** | Le système bloque un étudiant après cinq codes erronés | Quand j'ai saisi cinq codes erronés d'affilée, alors mes tentatives sont refusées par `429 TROP_D_ESSAIS` pendant deux minutes | Could |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| **ENF1** | L'écran de marquage de présence est utilisable sur un téléphone | Ouvrir l'écran étudiant dans un navigateur réduit à 360 px de large : le champ du code et le bouton restent accessibles sans défilement horizontal |
| **ENF2** | Le tableau du formateur répond en moins de 2 secondes pour une promotion de 60 étudiants | Charger le jeu de démonstration à 60 étudiants et mesurer le temps de réponse de `GET /api/tableau` ; il tient parce que le calcul est fait en une seule requête agrégée, pas en N+1 |
| **ENF3** | Volumétrie cible : 5 promotions, 60 étudiants chacune, 200 sessions par an | Le modèle ne comporte aucune table dont la taille croisse plus vite que le produit sessions × étudiants |
| **ENF4** | Toute erreur, sans exception, est renvoyée au format `{ code, message }` | Un test d'intégration provoque chaque cas d'erreur du contrat et vérifie la présence des deux champs ; aucune stack trace ni page d'erreur Spring par défaut ne sort de l'API |
| **ENF5** | L'application démarre chez un tiers en trois commandes au plus, sans configuration manuelle | Cloner le dépôt dans un dossier vide et suivre le `README` à la lettre, sans rien connaître du projet |
| **ENF6** | Le schéma de base est versionné et rejouable depuis zéro | Supprimer le volume Postgres et redémarrer : Flyway recrée le schéma et les données de démonstration à l'identique |
| **ENF7** | Les tests s'exécutent sur un poste vierge, sans base de données installée | `./mvnw test` passe sur une machine où Postgres n'est pas lancé |
| **ENF8** | Les messages d'erreur destinés à l'utilisateur sont en français | Relecture des libellés associés à chaque code d'erreur |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| **RG1** | Un code de présence expire 15 minutes après l'ouverture de la session | Q2 |
| **RG2** | Un étudiant ne peut jamais relire son propre exercice | Q5 |
| **RG3** | Une note est un nombre entier compris entre 0 et 20 inclus | Q9 |
| **RG4** | Un étudiant ne peut être présent qu'une seule fois à une même session | Contrat (`409 DEJA_PRESENT`) |
| **RG5** | Une présence ne peut pas être marquée après la fin de la session | Q3 |
| **RG6** | Le code d'une session est unique parmi les sessions non expirées et n'est pas déductible d'un autre | Q4, déduit |
| **RG7** | Un exercice reçoit un seul relecteur | Q6 |
| **RG8** | Le relecteur est tiré au sort par le système parmi les étudiants présents à la session, l'auteur exclu | Q7 + Q5 |
| **RG9** | Un étudiant ne dépose qu'un seul exercice par session | Contrat (`409 EXERCICE_DEJA_DEPOSE`) |
| **RG10** | Le lien d'un exercice est une URL `http` ou `https` syntaxiquement valide | Contrat (`400 LIEN_INVALIDE`) |
| **RG11** | Un exercice peut être déposé après la fin de la session, jusqu'à la clôture de celle-ci | Q12 |
| **RG12** | Le lien d'un exercice peut être remplacé tant que son relecteur ne l'a pas ouvert, c'est-à-dire tant que `relecture.consultee_at` est nul | Q13, moment précisé en section 7 |
| **RG13** | Une relecture validée est définitive : ni la note ni le commentaire ne peuvent plus être modifiés | Q15, contradiction avec Q10 tranchée en section 7 |
| **RG14** | L'auteur d'un exercice voit la note et le commentaire reçus, jamais l'identité de son relecteur | Q8 |
| **RG15** | Une présence ajoutée par le formateur porte la source `FORMATEUR` ; une présence saisie par l'étudiant porte la source `ETUDIANT` | Q14 |
| **RG16** | Un exercice dont la relecture n'a pas été rendue reste « en attente » et apparaît comme tel dans le tableau du formateur | Q11 |
| **RG17** | Après cinq codes erronés consécutifs, un étudiant est bloqué deux minutes | Q4 |
| **RG18** | La moyenne d'un étudiant est la moyenne des notes des relectures **rendues** portant sur ses exercices ; elle est nulle tant qu'aucune note n'a été reçue | Q16 + contrat (`moyenne` nullable) |
| **RG19** | Après la clôture d'une session par le formateur, plus aucun dépôt, remplacement de lien ni rendu de relecture n'y est accepté | Hypothèse, section 7 |
| **RG20** | Si aucun étudiant présent n'est éligible au tirage, l'exercice reste en attente d'assignation et le tirage est retenté au prochain dépôt ou à la prochaine présence enregistrée sur la session | Hypothèse, section 7 |
| **RG21** | Un étudiant appartient à une et une seule promotion | Déduit de Q16 et du contrat (`GET /api/tableau?promotionId=`) |
| **RG22** | La fin d'une session (expiration du code, 15 min) et sa clôture (acte du formateur) sont deux moments distincts | Hypothèse, section 7 |
| **RG23** | Un étudiant ne peut déposer un exercice que pour une session à laquelle sa présence est enregistrée | Hypothèse, section 7 |

## 7. Zones d'ombre, hypothèses et contradictions

### Points que la demande ne tranche pas

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|
| **La clôture de session n'existe nulle part.** Q10, Q12 et Q13 en dépendent toutes les trois, mais aucune des cinq opérations imposées ne la fournit et le client n'en parle jamais comme d'une fonctionnalité | Q10, Q12 — hypothèse | La clôture devient une opération explicite du formateur : `POST /api/sessions/{id}/cloture`. Une session est `OUVERTE` puis `CLOTUREE` | RG19, EF12. Une colonne `cloturee_at` sur `session`. C'est le principal ajout au contrat |
| **« Fin de la session » (Q3) et « clôture » (Q12) désignent-elles la même chose ?** Q3 interdit la présence après la fin, Q12 autorise le dépôt jusqu'à la clôture — si c'était le même moment, les deux réponses seraient incohérentes | Q3, Q12 — hypothèse | Deux notions distinctes. La **fin** est l'expiration du code, 15 minutes après l'ouverture, automatique. La **clôture** est un acte volontaire du formateur, plus tard | RG22. C'est ce qui rend Q3 et Q12 compatibles : on ne peut plus être présent après 15 min, mais on peut encore déposer le soir même |
| **Le référentiel des étudiants et des promotions n'existe pas.** Q1 dit « l'étudiant choisit son nom dans une liste », mais aucune opération ne fournit cette liste et rien ne dit qui crée les étudiants | Q1 — hypothèse | Référentiel en lecture seule, chargé par une migration de démonstration. Ajout de `GET /api/promotions` et `GET /api/promotions/{id}/etudiants` | EF2. Hors périmètre : la création et l'administration des étudiants |
| **Sur quoi s'accroche le blocage de Q4 ?** Cinq erreurs « et on le bloque » — mais sans authentification (Q1), il n'existe aucune identité fiable à bloquer | Q1 vs Q4 — hypothèse | Le compteur est tenu par `etudiantId` déclaré, et le refus renvoie `429 TROP_D_ESSAIS`. Protection de confort, pas de sécurité : sans authentification, elle est contournable en changeant d'identifiant, et nous l'écrivons plutôt que de laisser croire le contraire | RG17, EF16, priorité **Could** |
| **Quand le relecteur est-il tiré au sort ?** Q7 dit qui le choisit, jamais à quel moment | Q7 — hypothèse | Au moment du dépôt de l'exercice. C'est le seul instant où l'on connaît à la fois l'exercice à relire et la liste des présents | RG8. Le tirage est donc un effet de bord de `POST /api/exercices` |
| **Et s'il n'y a personne à tirer au sort ?** Q7 impose de choisir parmi les présents, Q5 exclut l'auteur : si l'auteur est le seul présent, l'ensemble est vide | Q5 + Q7 — hypothèse | L'exercice reste au statut `EN_ATTENTE_ASSIGNATION` et le tirage est retenté à chaque nouvelle présence ou nouveau dépôt sur la session. Aucun échec n'est renvoyé à l'étudiant, qui n'y peut rien | RG20. Statut supplémentaire dans le cycle de vie de l'exercice (diagramme D4) |
| **À partir de quand une relecture « a-t-elle commencé » ?** Q13 autorise le remplacement du lien « tant que personne n'a commencé à le relire », sans dire ce que cela veut dire. Comme le relecteur est assigné dès le dépôt (RG8), l'assignation ne peut pas être ce moment — sinon un remplacement n'aurait jamais été possible et Q13 serait sans objet | Q13 — hypothèse | Une relecture commence quand son relecteur **ouvre l'exercice pour la première fois**. Une colonne `consultee_at` porte cet instant | RG12, EF14 et l'état `EN_COURS_DE_RELECTURE` du diagramme D4. Le remplacement refusé renvoie `409 RELECTURE_COMMENCEE` |
| **Faut-il être présent à une session pour y déposer un exercice ?** La demande lie l'exercice à une session sans jamais dire si l'auteur devait y assister | Q7, Q12, Q14 — hypothèse | Oui, la présence est requise (RG23), et le dépôt sans présence renvoie `409 ETUDIANT_NON_PRESENT`. Trois appuis : Q7 ne tire les relecteurs que parmi les présents, donc la session est bien un ensemble de présents ; Q12 autorise un dépôt tardif mais parle de « certains n'ont pas de connexion le soir même », c'est-à-dire de gens qui étaient là ; et Q14 permet au formateur de rattraper une présence manquante, ce qui règle le cas du téléphone en panne sans ouvrir le dépôt à un absent | RG23. Un étudiant absent qui doit rendre son travail passe par le formateur, qui ajoute sa présence (EF13) |
| **Le relecteur est-il un acteur ou un état ?** Question posée par le modèle de cahier des charges, que la demande ne tranche pas | Hypothèse | Un étudiant dans un état, pas un acteur distinct. Justifié en section 2 | Aucune table `Relecteur` en D2 ; une clé `relecteur_id` sur `relecture` |
| **Que vaut la moyenne d'un étudiant qui n'a reçu aucune note ?** Q16 demande « la moyenne des notes reçues » sans dire ce qu'elle vaut quand il n'y en a pas | Q16 + contrat | `null`, et non `0`. Le contrat déclare d'ailleurs `moyenne` comme `nullable` | RG18. Un `0` affiché laisserait croire à un travail noté zéro, ce qui est faux et injuste |
| **Une relecture non rendue compte-t-elle dans la moyenne ?** Q11 décrit l'exercice « en attente » sans préciser son effet sur le calcul | Q11 + Q16 — hypothèse | Non. Seules les relectures rendues entrent dans la moyenne ; les autres alimentent le compteur `relecturesEnAttente` | RG16, RG18 |

### Contradictions relevées

| Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|
| **Q10** — « Un relecteur peut corriger sa note après l'avoir envoyée ? *Oui, tant que le formateur n'a pas clôturé la session.* »<br>**Q15** — « La note est-elle définitive une fois envoyée ? *Oui. Une fois que le relecteur a validé, c'est fini, il ne peut plus y revenir.* » | **Q15.** Une relecture validée est définitive (RG13). Q10 est réinterprété comme la possibilité de modifier une relecture **tant qu'elle n'est pas validée** | Trois raisons, dans cet ordre. **① Le contrat tranche déjà.** `POST /api/relectures/{id}` impose `409 RELECTURE_DEJA_RENDUE` : le client a arbitré dans le document contractuel, qui prime sur une réponse orale. Choisir Q10 obligerait à ne jamais renvoyer ce code et violerait la contrainte B2. **② Q15 est plus tardive et plus argumentée.** Le client y ajoute une justification — « c'est plus honnête pour tout le monde » — là où Q10 est une réponse de confort. Une intention motivée l'emporte sur une permission accordée sans réflexion. **③ Le coût de l'erreur est asymétrique.** Si nous nous trompons en figeant la note, le formateur peut toujours demander une évolution ; si nous nous trompons en la laissant modifiable, une note peut changer après que l'étudiant l'a lue, ce qui détruit la confiance que Q15 cherche précisément à protéger |

*Cette contradiction est celle que le sujet annonce. Nous la tranchons ici plutôt que de la contourner, et RG13 en porte la trace pour que les issues et les tests puissent la citer.*

## 8. Contraintes techniques

### Imposées par le sujet

| Réf | Contrainte | Comment nous la tenons |
|---|---|---|
| **B1** | Java 17+, Maven, wrapper `mvnw` commité | Compilation ciblée sur Java 17 (`maven.compiler.release`) bien que le poste tourne un JDK plus récent, pour garantir la compilation chez un tiers. Le wrapper est explicitement exclu du `.gitignore` |
| **B2** | `api/contrat.yaml` respecté à la lettre | Les cinq opérations imposées sont reprises sans la moindre modification. Nos ajouts sont de nouvelles opérations, jamais des altérations des existantes |
| **B3** | Séparation contrôleur / service / repository, DTO obligatoires | Aucune entité JPA n'est sérialisée en JSON ; chaque opération a ses DTO d'entrée et de sortie |
| **B4** | Validation des entrées, gestion centralisée des erreurs | `jakarta.validation` sur les DTO d'entrée, un `@RestControllerAdvice` unique qui traduit chaque exception métier en `{ code, message }` |
| **B5** | Schéma versionné, `ddl-auto=update` interdit hors tests | Flyway, migrations numérotées et commitées, `ddl-auto=validate` |
| **B6** | Un test unitaire sur une règle métier, un test d'intégration sur un endpoint | Test unitaire sur RG8 (le tirage au sort n'inclut jamais l'auteur) et test d'intégration sur les quatre codes d'erreur de `POST /api/presences` |
| **F1** | Framework déclaré et justifié, build qui passe | Angular, justifié en tête de ce document et repris dans le `README` |
| **F2** | Trois écrans : formateur, étudiant, relecteur | Trois routes distinctes |
| **F3** | Couche d'appels API dédiée, états de chargement et d'erreur, aucune règle métier dupliquée | Services Angular dédiés ; la moyenne affichée est celle que renvoie `GET /api/tableau`, elle n'est jamais recalculée côté navigateur |

### Que nous nous imposons

| Contrainte | Décision | Pourquoi |
|---|---|---|
| Base de données | **PostgreSQL 16**, lancé par `docker compose` | Une base réelle, proche de la production, et un démarrage en une commande chez le correcteur |
| Migrations | **Flyway**, SQL numéroté `V1__…`, `V2__…` | Imposé par B5, et surtout : l'étape 3 touchera le schéma. Une migration additive se relit en diff, un schéma généré ne se relit pas |
| Données de démonstration | Migration Flyway dédiée, pas un script manuel | Garantit que le correcteur n'ouvre jamais une application vide, et que les données se rejouent depuis zéro |
| Tests | **H2 en mode de compatibilité PostgreSQL**, mêmes migrations Flyway, aucune base externe | ENF7 et B6 exigent que les tests passent sur un poste vierge. Testcontainers a été écarté : il impose un démon Docker actif et le téléchargement d'une image, ce qui fait échouer `./mvnw test` hors ligne. Le prix à payer est de tenir les migrations en SQL portable, contrainte que nous acceptons et que nous documentons |
| Identifiants | Entiers `BIGINT` auto-incrémentés | Le contrat impose `integer, format: int64` sur tous les identifiants |
| Dates | `TIMESTAMP WITH TIME ZONE`, UTC en base | `ouvertureAt` et `expirationAt` sont des `date-time` au contrat, et RG1 est une règle d'écart temporel qu'un décalage de fuseau fausserait |
| Format d'erreur | Un unique énuméré de codes d'erreur côté serveur | Évite que deux endpoints renvoient deux orthographes du même code, ce que B2 sanctionne |

## 9. Livrables

| Livrable | Emplacement | Jalon |
|---|---|---|
| Ce cahier des charges, tenu à jour | `docs/CAHIER_DES_CHARGES.md` | `[JALON] analyse`, révisé après l'étape 3 |
| Diagramme de cas d'utilisation (D1) | `docs/diagrammes/D1-cas-utilisation.md` | `[JALON] analyse` |
| Modèle de données (D2), cohérent avec les migrations | `docs/diagrammes/D2-modele-donnees.md` | `[JALON] analyse`, révisé après l'étape 3 |
| Séquence « marquer sa présence » (D3), cohérente avec les codes HTTP du contrat | `docs/diagrammes/D3-sequence-presence.md` | `[JALON] analyse` |
| États-transitions du cycle de vie d'un exercice (D4) | `docs/diagrammes/D4-etats-exercice.md` | `[JALON] analyse` |
| Contrat d'API complété et figé avant tout code | `api/contrat.yaml` | `[JALON] analyse`, révisé après l'étape 3 |
| Backlog en issues, priorisées et rattachées aux `EFx` / `RGx` | Onglet Issues du dépôt | `[JALON] analyse` |
| Backend Spring Boot avec ses migrations et ses tests | `backend/` | `[JALON] v0.1` puis `[JALON] v1.0` |
| Frontend Angular, trois écrans | `frontend/` | `[JALON] v0.1` puis `[JALON] v1.0` |
| Journal de bord, une entrée par étape écrite sur le moment | `docs/JOURNAL.md` | tout au long |
| `README` d'installation testé depuis un clone vierge | `README.md` | `[JALON] v1.0` |
| `CHANGELOG` cohérent avec l'historique Git | `CHANGELOG.md` | `[JALON] v1.0` |
| Fichier de soumission | `SOUMISSION.md`, téléversé sur la plateforme | étape 5 |

## 10. Démarche prévue

L'épreuve se mène en cinq étapes, dans l'ordre, et cet ordre se lit dans l'historique Git.

| Étape | Ce que nous visons | Ce qui la clôt |
|---|---|---|
| **1 — Analyser** | Ce document, les quatre diagrammes, le contrat complété, le backlog en issues. Aucun code, pas même un `spring init` | Commit vide `[JALON] analyse`, poussé avant le premier fichier de code |
| **2 — Construire** | Les seules exigences **Must**. Une branche par issue, une PR par branche, chaque PR rattachée à son issue, `main` toujours en état de démarrer | Commit vide `[JALON] v0.1`, poussé |
| **3 — Encaisser le changement** | Demander l'enveloppe au surveillant dès `v0.1` poussé. Ouvrir une issue **avant** d'écrire la moindre ligne, reproduire le bug par un test qui échoue, versionner la migration, mettre à jour le contrat, puis corriger. Le correctif et l'évolution vivent dans deux branches et deux PR distinctes | Analyse mise à jour dans un commit qui le dit, et journal des révisions renseigné |
| **4 — Livrer** | Les **Should** dans la limite du temps, `CHANGELOG.md`, `README` testé depuis un clone vierge dans un dossier vide, backlog restant trié et assumé | Commit vide `[JALON] v1.0`, poussé |
| **5 — Soumettre** | `SOUMISSION.md` rempli, dépôt vérifié public en navigation privée, hash complet relevé **après** le dernier push | Téléversement sur la plateforme |

**Ce que nous faisons si nous prenons du retard.** Nous coupons dans le produit, jamais dans
l'analyse ni dans la discipline Git : l'application entière pèse 17 points, l'analyse 38 et Git 30.
L'ordre de sacrifice est arrêté d'avance : EF16 (blocage après cinq erreurs) d'abord, puis EF15
(consultation de sa note par l'étudiant), puis EF14 (remplacement du lien), puis EF13 (présence
ajoutée à la main). Les douze premières exigences ne se négocient pas : elles portent les cinq
opérations imposées du contrat. Une exigence abandonnée reste une issue ouverte, priorisée et
expliquée — pas une issue supprimée.

**Comment nous menons l'étape 3.** L'enveloppe touchera la base, le contrat et le frontend. Nous
arrivons donc à `v0.1` avec de la marge plutôt qu'en fin de journée, parce que l'enveloppe se
demande à un surveillant et non à un script.

**Definition of Done — une issue est terminée quand :**

- tous ses critères d'acceptation sont vérifiables et vérifiés sur l'application qui tourne ;
- la règle de gestion qu'elle cite est couverte par un test automatisé quand elle est testable ;
- le code respecte la séparation des couches et ne renvoie aucune entité JPA en JSON ;
- toute erreur qu'elle introduit sort au format `{ code, message }` avec le code HTTP du contrat ;
- la branche est fusionnée dans `main` par une pull request rattachée à l'issue ;
- `main` démarre encore après la fusion ;
- le contrat, ce cahier des charges et les diagrammes ont été mis à jour si l'issue les rend faux.

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | 25 septembre 2026 | Version initiale, rédigée à l'étape 1 avant tout code |
| 1.1 | 25 septembre 2026 | Le tracé des diagrammes D2 et D4 a révélé une zone d'ombre de plus : Q13 ne dit pas à partir de quand une relecture « a commencé ». Tranchée (première ouverture par le relecteur), RG12 précisée, EF8 complétée. Toujours à l'étape 1, avant tout code |
| 1.2 | 25 septembre 2026 | La rédaction du contrat d'API a mis au jour une hypothèse restée implicite : faut-il être présent pour déposer un exercice ? Tranchée (oui), RG23 ajoutée, EF6 complétée. Toujours à l'étape 1, avant tout code |
| 1.3 | 25 septembre 2026 | L'issue #10 (format d'erreur) a mis au jour une contradiction interne : ENF4 exige que **toute** erreur réponde en `{ code, message }`, mais le catalogue `CodeErreur` ne listait que les cas métier — ni route inconnue, ni verbe refusé, ni plantage. Ces cas étaient donc condamnés à sortir sur la page d'erreur par défaut de Spring, que la même ENF4 interdit. Cinq codes ajoutés (`ROUTE_INCONNUE`, `METHODE_NON_SUPPORTEE`, `MEDIA_NON_TRAITABLE`, `MEDIA_NON_ACCEPTE`, `ERREUR_INTERNE`), les cinq opérations imposées restant intactes. Étape 2 |
