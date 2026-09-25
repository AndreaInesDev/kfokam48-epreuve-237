# D1 — Diagramme de cas d'utilisation

Acteurs de Présence48 et ce que chacun peut faire. Chaque cas d'utilisation porte la référence
de l'exigence fonctionnelle correspondante du cahier des charges.

```mermaid
flowchart LR
    F(("Formateur"))
    E(("Étudiant"))
    R(("Relecteur<br/>etudiant assigne"))
    S(("Systeme"))

    subgraph SYS["Présence48"]
        direction TB

        subgraph GS["Gestion des sessions"]
            UC1["Ouvrir une session<br/>et obtenir un code<br/>EF1"]
            UC2["Clôturer une session<br/>EF12"]
        end

        subgraph GP["Présence"]
            UC3["Consulter les étudiants<br/>de sa promotion<br/>EF2"]
            UC4["Marquer sa présence<br/>avec un code<br/>EF3 · EF4"]
            UC5["Ajouter une présence<br/>à la main<br/>EF13"]
            UC6["Bloquer après<br/>5 codes erronés<br/>EF16"]
        end

        subgraph GE["Exercices"]
            UC7["Déposer le lien<br/>de son exercice<br/>EF5 · EF6"]
            UC8["Remplacer le lien<br/>de son exercice<br/>EF14"]
            UC9["Assigner un relecteur<br/>au hasard<br/>EF7"]
        end

        subgraph GR["Relecture"]
            UC10["Consulter les exercices<br/>à relire<br/>EF8"]
            UC11["Rendre une note<br/>et un commentaire<br/>EF9 · EF10"]
            UC12["Consulter la note reçue<br/>sans l'identité du relecteur<br/>EF15"]
        end

        UC13["Consulter le tableau<br/>récapitulatif<br/>EF11"]
    end

    F --> UC1
    F --> UC2
    F --> UC5
    F --> UC13

    E --> UC3
    E --> UC4
    E --> UC7
    E --> UC8
    E --> UC12

    R --> UC10
    R --> UC11

    S --> UC9
    S --> UC6

    UC7 -. "«déclenche»<br/>RG8" .-> UC9
    UC4 -. "«précède»<br/>RG8" .-> UC9
    UC4 -. "«compte les échecs»<br/>RG17" .-> UC6
    UC11 -. "«alimente»<br/>RG18" .-> UC13

    classDef acteur fill:#fff,stroke:#333,stroke-width:2px
    classDef auto fill:#f4f4f4,stroke:#777,stroke-dasharray:4 3
    class F,E,R,S acteur
    class UC6,UC9 auto
```

## Ce que le diagramme dit, et pourquoi

**Le relecteur apparaît comme un acteur distinct mais ne l'est pas dans le modèle.** Il figure ici
séparément de l'étudiant parce qu'il exerce des cas d'utilisation différents, mais la section 2 du
cahier des charges tranche que c'est **un étudiant dans un état** — celui d'avoir été tiré au sort.
C'est pour cela que son étiquette porte la mention *étudiant assigné*, et c'est pour cela qu'aucune
table `Relecteur` n'existe en D2.

**Le « Système » est un acteur au sens UML.** Deux cas d'utilisation ne sont déclenchés par
personne : l'assignation au hasard d'un relecteur (RG8) et le blocage après cinq échecs (RG17).
Les rattacher au formateur ou à l'étudiant serait faux — aucun des deux ne les commande. Ils sont
tracés en pointillé pour cette raison.

**Les quatre relations en pointillé sont les dépendances métier**, pas des appels techniques :
déposer un exercice déclenche le tirage au sort, marquer sa présence élargit l'ensemble des
relecteurs éligibles, une saisie de code alimente le compteur d'échecs, et une note rendue alimente
la moyenne du tableau.

## Ce que chaque acteur ne peut pas faire

Les interdictions ne se dessinent pas dans un diagramme de cas d'utilisation — c'est une limite du
formalisme, pas un oubli. Elles sont dans la troisième colonne du tableau des acteurs, section 2 du
cahier des charges : le relecteur ne choisit pas l'exercice qu'il relit (RG8), il ne relit jamais le
sien (RG2), il ne revient pas sur une relecture validée (RG13), et l'étudiant ne connaît jamais
l'identité de son relecteur (RG14).
