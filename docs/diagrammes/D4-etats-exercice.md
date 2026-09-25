# D4 — États et transitions : cycle de vie d'un exercice

*Quatrième diagramme, facultatif au sujet (bonus +3 points).*

> **Mis à jour à l'étape 3.** Un exercice étant désormais relu par deux pairs, il
> existe un état intermédiaire que ce diagramme ne prévoyait pas : une seule des
> deux relectures rendue, note affichée mais provisoire. `RELU` ne signifie plus
> « une relecture rendue » mais « les deux rendues ».

La colonne `exercice.statut` de D2 ne prend que les quatre valeurs de ce diagramme. `DEPOSE`
n'y figure pas : c'est un état de passage, jamais écrit en base.

```mermaid
stateDiagram-v2
    direction TB

    state "depot (etat de passage)" as Depot
    state "EN_ATTENTE_ASSIGNATION" as ATTENTE_ASSIGN
    state "EN_ATTENTE_RELECTURE" as ATTENTE_RELEC
    state "EN_COURS_DE_RELECTURE" as EN_COURS
    state "RELU_PARTIELLEMENT" as PARTIEL
    state "RELU" as RELU

    [*] --> Depot : POST /api/exercices<br/>lien valide, session non cloturee<br/>EF5 · RG10 · RG11

    Depot --> ATTENTE_ASSIGN : aucun étudiant présent éligible<br/>(l'auteur est le seul présent)<br/>RG20
    Depot --> ATTENTE_RELEC : tirage au sort réussi<br/>un présent ≠ l'auteur<br/>RG8 · RG2

    ATTENTE_ASSIGN --> ATTENTE_RELEC : nouvelle présence sur la session<br/>le tirage est retenté<br/>RG20

    ATTENTE_RELEC --> EN_COURS : le relecteur ouvre l'exercice<br/>consultee_at est posé<br/>RG12

    EN_COURS --> PARTIEL : un seul des deux relecteurs a rendu<br/>note affichée mais PROVISOIRE<br/>RG18 · étape 3
    ATTENTE_RELEC --> PARTIEL : rendu direct, sans consultation préalable<br/>EF9 · RG3
    PARTIEL --> RELU : le second relecteur rend à son tour<br/>la note devient la moyenne des deux<br/>RG7 · RG18

    RELU --> [*]

    note right of ATTENTE_ASSIGN
        Le lien peut être remplacé (EF14 · RG12).
        Compte dans relecturesEnAttente du
        tableau formateur (RG16).
    end note

    note right of ATTENTE_RELEC
        Le lien peut encore être remplacé :
        personne n'a ouvert l'exercice.
        Compte dans relecturesEnAttente (RG16).
    end note

    note right of EN_COURS
        Le lien ne peut plus être remplacé :
        409 RELECTURE_COMMENCEE (EF14).
        Compte encore dans relecturesEnAttente.
    end note

    note right of PARTIEL
        Un seul des deux relecteurs a rendu.
        La note est affichée mais marquée
        PROVISOIRE (étape 3, RG18).
    end note

    note right of RELU
        État terminal : les deux relectures
        sont rendues, la note retenue est
        leur moyenne (RG7, RG18). Aucune
        des deux ne change plus (RG13).
    end note
```

## Ce que le diagramme rend visible

**`RELU` est terminal, et c'est la contradiction Q10/Q15 rendue graphique.** Aucune flèche ne sort de
`RELU` vers un état antérieur. Si nous avions tranché en faveur de Q10, il y aurait ici une
transition `RELU → EN_COURS` conditionnée par « la session n'est pas clôturée » — et l'API devrait
accepter un second `POST /api/relectures/{id}` au lieu de renvoyer le `409 RELECTURE_DEJA_RENDUE` que
le contrat impose. Le diagramme et le contrat disent donc la même chose, ce qui est précisément
l'objet du contrôle de cohérence.

**La clôture de session n'est pas un état de l'exercice.** On pourrait croire qu'un exercice devrait
passer à un état « clôturé » quand le formateur clôture la session. Ce serait une erreur de
modélisation : la clôture est un état de la **session**, et elle interdit certaines transitions
(plus de dépôt, plus de rendu — RG19) sans changer l'état des exercices existants. Un exercice resté
en `EN_ATTENTE_RELECTURE` dans une session clôturée n'a pas été relu, et le tableau du formateur doit
continuer à le dire (RG16). Représenter la clôture ici aurait effacé cette information.

**`EN_ATTENTE_ASSIGNATION` n'est pas un état d'erreur.** L'étudiant reçoit un `201` dans tous les cas :
il a bien déposé son exercice, et le fait qu'aucun relecteur n'ait pu être tiré au sort ne lui est pas
imputable et ne le concerne pas. C'est un état d'attente du système, visible du formateur seul.

**Il y a deux chemins vers `RELU`.** Un relecteur consciencieux ouvre l'exercice puis rend sa note, et
passe par `EN_COURS`. Mais rien n'oblige à consulter avant de rendre : la transition directe
`EN_ATTENTE_RELECTURE → RELU` existe pour que le modèle ne mente pas sur ce qui est techniquement
possible. Les deux chemins aboutissent au même état terminal.

## Les trois états que l'on compte dans le tableau

`relecturesEnAttente`, dans `GET /api/tableau`, dénombre pour chaque étudiant les relectures **qu'il
doit rendre** — donc les exercices d'autrui qui lui sont assignés et se trouvent dans l'un des états
`EN_ATTENTE_ASSIGNATION` (non, par définition : aucun relecteur n'est encore assigné),
`EN_ATTENTE_RELECTURE` ou `EN_COURS_DE_RELECTURE`. Autrement dit : les lignes de `relecture` dont il
est le relecteur et dont `rendue_at` est nul. C'est exactement ce que dit Q16 — « les relectures
qu'il doit encore faire » — et ce que RG16 formalise.
