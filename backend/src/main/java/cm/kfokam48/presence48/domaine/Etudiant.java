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

/**
 * Un etudiant, rattache a une seule promotion (RG21).
 *
 * <p>Il n'existe deliberement aucune entite Relecteur : le relecteur est un
 * etudiant dans un etat, pas un acteur distinct (section 2 du cahier des
 * charges). Le role est porte par {@code Relecture.relecteur}.
 *
 * <p>Correspond a la table {@code etudiant} de {@code V1__schema_initial.sql}.
 */
@Entity
@Table(name = "etudiant")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    /** RG21 : un etudiant appartient a une et une seule promotion. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    protected Etudiant() {
        // requis par JPA
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public Promotion getPromotion() {
        return promotion;
    }
}
