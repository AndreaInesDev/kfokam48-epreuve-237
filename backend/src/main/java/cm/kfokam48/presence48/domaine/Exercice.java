package cm.kfokam48.presence48.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

/**
 * Le lien de l'exercice rendu par un etudiant pour une session.
 *
 * <p>RG9 — un seul exercice par etudiant et par session, tenu par la contrainte
 * d'unicite en base.
 */
@Entity
@Table(name = "exercice",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_exercice_session_etudiant",
                columnNames = {"session_id", "etudiant_id"}))
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionCours session;

    /** L'auteur. RG2 le compare au relecteur pour interdire l'auto-relecture. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Column(name = "lien", nullable = false, length = 500)
    private String lien;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    private StatutExercice statut;

    @Column(name = "depose_at", nullable = false)
    private OffsetDateTime deposeAt;

    protected Exercice() {
        // requis par JPA
    }

    public Exercice(SessionCours session, Etudiant etudiant, String lien,
            StatutExercice statut, OffsetDateTime deposeAt) {
        this.session = session;
        this.etudiant = etudiant;
        this.lien = lien;
        this.statut = statut;
        this.deposeAt = deposeAt;
    }

    public void changerStatut(StatutExercice statut) {
        this.statut = statut;
    }

    /** RG12 — le remplacement du lien n'est possible qu'avant le debut de la relecture. */
    public void remplacerLien(String lien) {
        this.lien = lien;
    }

    public Long getId() {
        return id;
    }

    public SessionCours getSession() {
        return session;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public String getLien() {
        return lien;
    }

    public StatutExercice getStatut() {
        return statut;
    }

    public OffsetDateTime getDeposeAt() {
        return deposeAt;
    }
}
