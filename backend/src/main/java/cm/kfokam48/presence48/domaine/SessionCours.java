package cm.kfokam48.presence48.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Une session de cours, ouverte par le formateur, portant un code de presence.
 *
 * <p>Nommee SessionCours et mappee sur {@code session_cours} : SESSION est un
 * mot reserve du standard SQL, que H2 en mode PostgreSQL ne tolere pas toujours
 * comme nom de table — or les tests rejouent les memes migrations (ENF7).
 *
 * <p>Deux moments distincts, et c'est la decision RG22 : la <b>fin</b> de la
 * session est l'expiration automatique du code, quinze minutes apres
 * l'ouverture (RG1) ; la <b>cloture</b> est un acte volontaire du formateur,
 * plus tard (RG19). C'est ce qui rend Q3 et Q12 compatibles.
 */
@Entity
@Table(name = "session_cours")
public class SessionCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    /** RG6 : unique et non devinable. */
    @Column(name = "code", nullable = false, length = 12, unique = true)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private OffsetDateTime ouvertureAt;

    /** RG1 : ouvertureAt + duree de validite du code. */
    @Column(name = "expiration_at", nullable = false)
    private OffsetDateTime expirationAt;

    /** RG19, RG22 : nul tant que la session est ouverte. */
    @Column(name = "cloturee_at")
    private OffsetDateTime clotureeAt;

    protected SessionCours() {
        // requis par JPA
    }

    public SessionCours(String titre, Promotion promotion, String code,
            OffsetDateTime ouvertureAt, OffsetDateTime expirationAt) {
        this.titre = titre;
        this.promotion = promotion;
        this.code = code;
        this.ouvertureAt = ouvertureAt;
        this.expirationAt = expirationAt;
    }

    /** RG1 — le code ne fonctionne plus passe {@code expirationAt}. */
    public boolean codeExpireA(OffsetDateTime instant) {
        return instant.isAfter(expirationAt);
    }

    /** RG19 — apres la cloture, plus aucun depot ni rendu n'est accepte. */
    public boolean estCloturee() {
        return clotureeAt != null;
    }

    /**
     * RG19 — le formateur cloture la session. Irreversible : rien dans la
     * demande du client ne prevoit de reouvrir une session, et permettre
     * l'inverse reviendrait a rendre modifiables des relectures deja rendues,
     * ce que RG13 interdit.
     */
    public void cloturer(OffsetDateTime instant) {
        this.clotureeAt = instant;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public String getCode() {
        return code;
    }

    public OffsetDateTime getOuvertureAt() {
        return ouvertureAt;
    }

    public OffsetDateTime getExpirationAt() {
        return expirationAt;
    }

    public OffsetDateTime getClotureeAt() {
        return clotureeAt;
    }
}
