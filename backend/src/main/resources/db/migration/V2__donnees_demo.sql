-- =============================================================================
--  Presence48 — donnees de demonstration
--
--  Le sujet l'exige : « prevois quelques donnees de demonstration chargees au
--  demarrage, sinon le correcteur ouvre une application vide et ne peut rien
--  verifier ». Chargees par migration et non par un script manuel, pour qu'elles
--  se rejouent a l'identique quand on supprime le volume Postgres (ENF6).
--
--  Ce jeu est construit pour que le tableau du formateur montre TOUS les cas
--  interessants des la premiere ouverture :
--    - des moyennes nulles et non zero, chez des etudiants sans note (RG18)
--    - des relectures en attente, chez des etudiants qui en doivent (RG16)
--    - une presence ajoutee par le formateur (RG15, Q14)
--    - un exercice sans relecteur, faute de present eligible (RG20)
--    - une session cloturee, une session expiree mais ouverte (RG11, RG22)
--    - une session ouverte a l'instant, dont le code est utilisable 15 minutes
--
--  Intervalles ecrits sous la forme normalisee INTERVAL '2' DAY, comprise
--  par PostgreSQL comme par H2 : les tests rejouent ce fichier (ENF7).
-- =============================================================================

-- -----------------------------------------------------------------------------
--  Promotions et etudiants
-- -----------------------------------------------------------------------------
INSERT INTO promotion (id, nom) VALUES
  (1, 'Promotion 2026 — Yaounde'),
  (2, 'Promotion 2026 — Douala');

INSERT INTO etudiant (id, nom, promotion_id) VALUES
  (1,  'Abena Marceline',     1),
  (2,  'Bikoi Serge',         1),
  (3,  'Chatue Nadege',       1),
  (4,  'Djomo Patrick',       1),
  (5,  'Essomba Clarisse',    1),
  (6,  'Fongang Bertrand',    1),
  (7,  'Guemo Sylvie',        1),
  (8,  'Hamadou Ibrahim',     1),
  (9,  'Kamdem Josiane',      1),
  (10, 'Lobe Emmanuel',       1),
  (11, 'Manga Prisca',        1),
  (12, 'Ndongo Alain',        1),
  (13, 'Oyono Beatrice',      2),
  (14, 'Pokam Herve',         2),
  (15, 'Sadjo Aminatou',      2),
  (16, 'Tchuente Rodrigue',   2),
  (17, 'Voufo Carine',        2),
  (18, 'Wanko Gaston',        2),
  (19, 'Yebga Solange',       2),
  (20, 'Zoa Dieudonne',       2);

-- -----------------------------------------------------------------------------
--  Sessions
--   1 — cloturee : plus aucun depot ni rendu possible (RG19)
--   2 — code expire mais session NON cloturee : le depot reste possible (RG11)
--   3 — ouverte a l'instant : son code fonctionne 15 minutes (RG1)
--   4 — cloturee, sur l'autre promotion
-- -----------------------------------------------------------------------------
INSERT INTO session_cours (id, titre, promotion_id, code, ouverture_at, expiration_at, cloturee_at) VALUES
  (1, 'Java — les collections', 1, 'JAVA-COL1',
      CURRENT_TIMESTAMP - INTERVAL '2' DAY,
      CURRENT_TIMESTAMP - INTERVAL '2' DAY + INTERVAL '15' MINUTE,
      CURRENT_TIMESTAMP - INTERVAL '1' DAY),
  (2, 'Spring Boot — les couches', 1, 'SPRG-LAY2',
      CURRENT_TIMESTAMP - INTERVAL '1' DAY,
      CURRENT_TIMESTAMP - INTERVAL '1' DAY + INTERVAL '15' MINUTE,
      NULL),
  (3, 'Angular — les services', 1, 'ANGU-SRV3',
      CURRENT_TIMESTAMP,
      CURRENT_TIMESTAMP + INTERVAL '15' MINUTE,
      NULL),
  (4, 'Git — reecrire l''historique', 2, 'GIT-HIST4',
      CURRENT_TIMESTAMP - INTERVAL '3' DAY,
      CURRENT_TIMESTAMP - INTERVAL '3' DAY + INTERVAL '15' MINUTE,
      CURRENT_TIMESTAMP - INTERVAL '2' DAY);

-- -----------------------------------------------------------------------------
--  Presences
--  La presence 8 porte source = FORMATEUR : Hamadou Ibrahim avait un souci de
--  telephone, le formateur l'a ajoute a la main (Q14, RG15).
-- -----------------------------------------------------------------------------
INSERT INTO presence (id, session_id, etudiant_id, source, marquee_at) VALUES
  (1,  1, 1, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (2,  1, 2, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (3,  1, 3, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (4,  1, 4, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (5,  1, 5, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (6,  1, 6, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (7,  1, 7, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (8,  1, 8, 'FORMATEUR', CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (9,  2, 1, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '1' DAY),
  (10, 2, 2, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '1' DAY),
  (11, 2, 3, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '1' DAY),
  (12, 2, 4, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '1' DAY),
  (13, 2, 5, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '1' DAY),
  (14, 2, 6, 'ETUDIANT',  CURRENT_TIMESTAMP - INTERVAL '1' DAY),
  (15, 4, 13, 'ETUDIANT', CURRENT_TIMESTAMP - INTERVAL '3' DAY),
  (16, 4, 14, 'ETUDIANT', CURRENT_TIMESTAMP - INTERVAL '3' DAY),
  (17, 4, 15, 'ETUDIANT', CURRENT_TIMESTAMP - INTERVAL '3' DAY),
  (18, 4, 16, 'ETUDIANT', CURRENT_TIMESTAMP - INTERVAL '3' DAY),
  (19, 4, 17, 'ETUDIANT', CURRENT_TIMESTAMP - INTERVAL '3' DAY);

-- -----------------------------------------------------------------------------
--  Exercices
--  L'exercice 8 est en EN_ATTENTE_ASSIGNATION : il illustre RG20, le cas ou le
--  tirage au sort n'a trouve personne au moment du depot.
-- -----------------------------------------------------------------------------
INSERT INTO exercice (id, session_id, etudiant_id, lien, statut, depose_at) VALUES
  (1,  1, 1, 'https://github.com/demo/collections-abena',   'RELU',
       CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (2,  1, 2, 'https://github.com/demo/collections-bikoi',   'RELU',
       CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (3,  1, 3, 'https://github.com/demo/collections-chatue',  'RELU',
       CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (4,  1, 4, 'https://github.com/demo/collections-djomo',   'EN_COURS_DE_RELECTURE',
       CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (5,  1, 5, 'https://github.com/demo/collections-essomba', 'EN_ATTENTE_RELECTURE',
       CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (6,  2, 1, 'https://github.com/demo/couches-abena',       'RELU',
       CURRENT_TIMESTAMP - INTERVAL '1' DAY),
  (7,  2, 2, 'https://github.com/demo/couches-bikoi',       'EN_ATTENTE_RELECTURE',
       CURRENT_TIMESTAMP - INTERVAL '1' DAY),
  (8,  2, 3, 'https://github.com/demo/couches-chatue',      'EN_ATTENTE_ASSIGNATION',
       CURRENT_TIMESTAMP - INTERVAL '1' DAY),
  (9,  4, 13, 'https://github.com/demo/git-oyono',          'RELU',
       CURRENT_TIMESTAMP - INTERVAL '3' DAY),
  (10, 4, 14, 'https://github.com/demo/git-pokam',          'EN_ATTENTE_RELECTURE',
       CURRENT_TIMESTAMP - INTERVAL '3' DAY);

-- -----------------------------------------------------------------------------
--  Relectures
--  Aucun relecteur n'est l'auteur de l'exercice qu'il relit (RG2), et tous
--  etaient presents a la session concernee (RG8).
--  L'exercice 8 n'a volontairement aucune ligne ici (RG20).
-- -----------------------------------------------------------------------------
INSERT INTO relecture (id, exercice_id, relecteur_id, note, commentaire, assignee_at, consultee_at, rendue_at) VALUES
  (1, 1, 2, 15, 'Bonne maitrise des Map, mais les tests manquent sur les cas limites.',
      CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY,
      CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (2, 2, 3, 12, 'Le code fonctionne mais les noms de variables restent obscurs.',
      CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY,
      CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  (3, 3, 4, 18, 'Tres propre. La separation des responsabilites est exemplaire.',
      CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY,
      CURRENT_TIMESTAMP - INTERVAL '2' DAY),
  -- ouverte mais pas encore rendue : l'auteur ne peut plus changer son lien (RG12)
  (4, 4, 5, NULL, NULL,
      CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY, NULL),
  -- assignee, jamais ouverte : l'auteur peut encore changer son lien (RG12)
  (5, 5, 6, NULL, NULL,
      CURRENT_TIMESTAMP - INTERVAL '2' DAY, NULL, NULL),
  (6, 6, 4, 9, 'Les couches sont melangees : le controleur interroge la base directement.',
      CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY,
      CURRENT_TIMESTAMP - INTERVAL '1' DAY),
  (7, 7, 5, NULL, NULL,
      CURRENT_TIMESTAMP - INTERVAL '1' DAY, NULL, NULL),
  (8, 9, 14, 16, 'Le rebase interactif est bien compris, le message de commit gagnerait a etre plus precis.',
      CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY,
      CURRENT_TIMESTAMP - INTERVAL '3' DAY),
  (9, 10, 15, NULL, NULL,
      CURRENT_TIMESTAMP - INTERVAL '3' DAY, NULL, NULL);

-- -----------------------------------------------------------------------------
--  Remise a niveau des sequences d'identite.
--  Les identifiants ci-dessus sont explicites pour rester lisibles ; sans ces
--  RESTART, la premiere insertion faite par l'application repartirait de 1 et
--  violerait les cles primaires.
-- -----------------------------------------------------------------------------
ALTER TABLE promotion     ALTER COLUMN id RESTART WITH 3;
ALTER TABLE etudiant      ALTER COLUMN id RESTART WITH 21;
ALTER TABLE session_cours ALTER COLUMN id RESTART WITH 5;
ALTER TABLE presence      ALTER COLUMN id RESTART WITH 20;
ALTER TABLE exercice      ALTER COLUMN id RESTART WITH 11;
ALTER TABLE relecture     ALTER COLUMN id RESTART WITH 10;
