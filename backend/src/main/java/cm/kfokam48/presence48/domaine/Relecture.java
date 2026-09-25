package cm.kfokam48.presence48.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * La relecture d'un exercice par un pair.
 *
 * <p>Il n'existe aucune entite Relecteur : le relecteur est un etudiant dans un
 * etat (section 2 du cahier des charges), et ce role est porte par
 * {@link #relecteur}. C'est la seule modelisation qui permette d'exprimer RG2,
 * qui compare l'auteur et le relecteur comme deux references au meme referentiel.
 *
 * <p>La ligne est creee des le tirage au sort, avec note, commentaire,
 * {@code consulteeAt} et {@code rendueAt} a nul : c'est ce qui permet de compter
 * les relectures en attente du tableau formateur (RG16).
 */
@Entity
@Table(name = "relecture")
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** RG7 — un seul relecteur par exercice, garanti par l'unicite en base. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercice_id", nullable = false, unique = true)
    private Exercice exercice;

    /** RG2, RG8 — jamais l'auteur de l'exercice. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relecteur_id", nullable = false)
    private Etudiant relecteur;

    /** RG3 — entier de 0 a 20, nul jusqu'au rendu. */
    @Column(name = "note")
    private Integer note;

    @Column(name = "commentaire", length = 2000)
    private String commentaire;

    @Column(name = "assignee_at", nullable = false)
    private OffsetDateTime assigneeAt;

    /** RG12 — instant ou la relecture commence : le relecteur ouvre l'exercice. */
    @Column(name = "consultee_at")
    private OffsetDateTime consulteeAt;

    /** RG13 — une fois pose, la relecture est definitive. */
    @Column(name = "rendue_at")
    private OffsetDateTime rendueAt;

    protected Relecture() {
        // requis par JPA
    }

    public Relecture(Exercice exercice, Etudiant relecteur, OffsetDateTime assigneeAt) {
        this.exercice = exercice;
        this.relecteur = relecteur;
        this.assigneeAt = assigneeAt;
    }

    /** RG12 — premiere ouverture seulement : l'instant ne bouge plus ensuite. */
    public void ouvrir(OffsetDateTime instant) {
        if (consulteeAt == null) {
            this.consulteeAt = instant;
        }
    }

    /** RG13 — rend la relecture. Definitif. */
    public void rendre(int note, String commentaire, OffsetDateTime instant) {
        this.note = note;
        this.commentaire = commentaire;
        this.rendueAt = instant;
    }

    /** RG12 — vrai des que le relecteur a ouvert l'exercice. */
    public boolean estCommencee() {
        return consulteeAt != null;
    }

    /** RG13, RG16 — vrai une fois la note validee. */
    public boolean estRendue() {
        return rendueAt != null;
    }

    public Long getId() {
        return id;
    }

    public Exercice getExercice() {
        return exercice;
    }

    public Etudiant getRelecteur() {
        return relecteur;
    }

    public Integer getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public OffsetDateTime getConsulteeAt() {
        return consulteeAt;
    }

    public OffsetDateTime getRendueAt() {
        return rendueAt;
    }
}
