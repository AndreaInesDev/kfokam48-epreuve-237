-- =============================================================================
--  Etape 3 — changement de besoin : deux relecteurs par exercice
--
--  Le client a change d'avis sur Q6 : « un seul relecteur ca ne marche pas :
--  quand il ne rend rien, l'etudiant n'a aucune note. A partir de maintenant,
--  chaque exercice est relu par deux pairs differents, et la note retenue est la
--  moyenne des deux. »
--
--  MIGRATION AJOUTEE, JAMAIS MODIFIEE EN PLACE. V1 et V2 restent telles quelles :
--  une base deja remplie doit survivre a ce changement, et les relectures
--  existantes sont conservees — elles deviennent simplement la premiere des deux.
--
--  SQL portable PostgreSQL / H2, comme les precedentes (ENF7).
-- =============================================================================

-- -----------------------------------------------------------------------------
--  RG7 devient fausse : « un seul relecteur par exercice » devient « deux ».
--  La contrainte d'unicite qui la tenait doit donc disparaitre. C'est le seul
--  changement destructif de cette migration, et il est indispensable : sans lui,
--  la seconde relecture d'un exercice serait refusee par la base.
-- -----------------------------------------------------------------------------
ALTER TABLE relecture DROP CONSTRAINT uk_relecture_exercice;

-- -----------------------------------------------------------------------------
--  RG7bis : un meme etudiant ne peut pas etre les DEUX relecteurs d'un exercice.
--  Le client demande « deux pairs differents » ; sans cette contrainte, le
--  tirage au sort pourrait designer deux fois la meme personne et l'on aurait
--  deux notes du meme relecteur, ce qui viderait la regle de son sens.
-- -----------------------------------------------------------------------------
ALTER TABLE relecture
    ADD CONSTRAINT uk_relecture_exercice_relecteur UNIQUE (exercice_id, relecteur_id);

-- -----------------------------------------------------------------------------
--  Index de lecture : le tableau du formateur agrege desormais plusieurs
--  relectures par exercice, et non plus au plus une.
-- -----------------------------------------------------------------------------
CREATE INDEX idx_relecture_exercice ON relecture (exercice_id);

-- -----------------------------------------------------------------------------
--  Donnees existantes : rien a reprendre.
--
--  Les nouveaux exercices recevront deux relecteurs au depot. Les exercices deja
--  deposes n'en ont qu'un, et c'est correct au regard de la nouvelle regle : leur
--  note est simplement PROVISOIRE tant que le second relecteur n'existe pas.
--  C'est exactement le cas que le client decrit — « si un seul des deux a rendu,
--  on affiche sa note en attendant, mais marquee comme provisoire ».
--
--  Aucun UPDATE n'est donc necessaire, et c'est voulu : fabriquer de faux seconds
--  relecteurs pour l'historique serait inventer des donnees.
-- -----------------------------------------------------------------------------
