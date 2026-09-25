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
 * La presence d'un etudiant a une session.
 *
 * <p>RG4 — un etudiant n'est present qu'une fois a une meme session. La regle
 * est tenue par la contrainte d'unicite en base, pas seulement par le service :
 * c'est ce qui garantit le 409 meme en cas d'appels concurrents.
 */
@Entity
@Table(name = "presence",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_presence_session_etudiant",
                columnNames = {"session_id", "etudiant_id"}))
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionCours session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    /** RG15 — ETUDIANT ou FORMATEUR. Stocke en clair pour rester lisible en base. */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 9)
    private SourcePresence source;

    @Column(name = "marquee_at", nullable = false)
    private OffsetDateTime marqueeAt;

    protected Presence() {
        // requis par JPA
    }

    public Presence(SessionCours session, Etudiant etudiant, SourcePresence source,
            OffsetDateTime marqueeAt) {
        this.session = session;
        this.etudiant = etudiant;
        this.source = source;
        this.marqueeAt = marqueeAt;
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

    public SourcePresence getSource() {
        return source;
    }

    public OffsetDateTime getMarqueeAt() {
        return marqueeAt;
    }
}
