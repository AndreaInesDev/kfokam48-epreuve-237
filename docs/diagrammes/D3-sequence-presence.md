# D3 — Séquence : marquer sa présence

Cas nominal et **tous** les cas d'erreur de `POST /api/presences`. Les codes de statut et les codes
d'erreur de ce diagramme sont ceux de `api/contrat.yaml`, à la lettre : c'est la cohérence entre les
deux que le barème vérifie.

```mermaid
sequenceDiagram
    autonumber
    actor E as Étudiant
    participant UI as Écran étudiant<br/>(Angular)
    participant API as PresenceController
    participant SRV as PresenceService
    participant REPO as Repositories
    participant DB as PostgreSQL

    E->>UI: choisit son nom, saisit le code
    UI->>API: POST /api/presences<br/>{ code, etudiantId }

    alt champ manquant ou mal formé
        API-->>UI: 400 { code: "REQUETE_INVALIDE" }
        UI-->>E: « Code et étudiant sont obligatoires »
    else requête valide
        API->>SRV: enregistrer(code, etudiantId)

        alt étudiant bloqué — 5 échecs récents (RG17, EF16)
            SRV-->>API: TropDEssaisException
            API-->>UI: 429 { code: "TROP_D_ESSAIS" }
            UI-->>E: « Trop d'essais, réessaie dans 2 minutes »
        else étudiant non bloqué
            SRV->>REPO: chercherSessionParCode(code)
            REPO->>DB: SELECT ... FROM session_cours WHERE code = ?
            DB-->>REPO: 0 ou 1 ligne
            REPO-->>SRV: Optional~SessionCours~

            alt code inconnu
                SRV->>REPO: enregistrerEchec(etudiantId) (RG17)
                SRV-->>API: CodeInconnuException
                API-->>UI: 400 { code: "CODE_INCONNU" }
                UI-->>E: « Ce code n'existe pas »
            else code trouvé mais expiré (RG1)
                Note over SRV: maintenant > expiration_at<br/>(ouverture + 15 min)
                SRV-->>API: CodeExpireException
                API-->>UI: 410 { code: "CODE_EXPIRE" }
                UI-->>E: « Le code de présence a expiré »
            else code valide et non expiré
                SRV->>REPO: existePresence(sessionId, etudiantId)
                REPO->>DB: SELECT 1 FROM presence<br/>WHERE session_id = ? AND etudiant_id = ?
                DB-->>REPO: présente ou absente
                REPO-->>SRV: booléen

                alt déjà présent (RG4)
                    SRV-->>API: DejaPresentException
                    API-->>UI: 409 { code: "DEJA_PRESENT" }
                    UI-->>E: « Ta présence est déjà enregistrée »
                else cas nominal
                    SRV->>REPO: enregistrer(Presence source=ETUDIANT) (RG15)
                    REPO->>DB: INSERT INTO presence ...
                    DB-->>REPO: id généré
                    REPO-->>SRV: Presence
                    SRV->>SRV: retenter le tirage au sort des<br/>exercices en attente d'assignation<br/>de cette session (RG20)
                    SRV-->>API: Presence
                    API-->>UI: 201 { id, sessionId, etudiantId, source: "ETUDIANT" }
                    UI-->>E: « Présence enregistrée »
                end
            end
        end
    end
```

## Correspondance avec le contrat d'API

| Situation | Code HTTP | Code d'erreur | Règle | Au contrat |
|---|---:|---|---|---|
| Présence enregistrée | `201` | — | RG15 | Imposé |
| Champ absent ou mal formé | `400` | `REQUETE_INVALIDE` | — | Ajouté (B4) |
| Code inexistant | `400` | `CODE_INCONNU` | — | Imposé |
| Code de plus de 15 minutes | `410` | `CODE_EXPIRE` | RG1 | Imposé |
| Étudiant déjà présent à cette session | `409` | `DEJA_PRESENT` | RG4 | Imposé |
| Cinq codes erronés consécutifs | `429` | `TROP_D_ESSAIS` | RG17 | Ajouté (Q4) |

## L'ordre des vérifications est un choix, pas un hasard

Les contrôles s'enchaînent dans cet ordre précis : **validation → blocage → code inconnu →
expiration → doublon**. Trois raisons.

**Le blocage passe avant la recherche du code.** RG17 existe pour empêcher de deviner les codes par
essais successifs (« sinon ils vont deviner les codes entre eux », Q4). Si l'on cherchait le code
avant de vérifier le blocage, un étudiant bloqué apprendrait quand même si son code existe — et la
règle ne servirait plus à rien.

**« Inconnu » passe avant « expiré ».** On ne peut pas dire qu'un code est expiré tant qu'on ne l'a
pas trouvé. C'est aussi ce qui explique l'apparente étrangeté du contrat, qui répond `400` pour un
code inconnu et `410` pour un code expiré : `410 Gone` signifie « cette ressource a existé et
n'existe plus », ce qui n'a de sens que pour un code réellement émis.

**« Déjà présent » passe en dernier.** Répondre `409` sur un code expiré renseignerait l'étudiant sur
la validité passée du code sans qu'il ait eu besoin d'un code valide. L'expiration est une propriété
du code, le doublon une propriété de la relation entre l'étudiant et la session : on vérifie le code
d'abord, la relation ensuite.

## Un effet de bord assumé

Le cas nominal se termine par une action qui n'a rien à voir avec la présence : **retenter le tirage
au sort des exercices en attente d'assignation** (RG20). C'est la conséquence directe de la zone
d'ombre tranchée en section 7 — quand un exercice a été déposé alors qu'aucun autre étudiant n'était
présent, l'arrivée d'un nouveau présent rend le tirage possible. Le faire ici est le seul moyen de
ne pas laisser un exercice orphelin, et cela ne peut pas échouer du point de vue de l'étudiant qui
marque sa présence : si le tirage échoue encore, l'exercice reste simplement en attente.
