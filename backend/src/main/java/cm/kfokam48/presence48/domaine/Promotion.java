package cm.kfokam48.presence48.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Une promotion d'etudiants. Referentiel en lecture seule pour l'application :
 * sa creation est hors perimetre (section 3 du cahier des charges), les donnees
 * viennent de la migration de demonstration.
 *
 * <p>Correspond a la table {@code promotion} de {@code V1__schema_initial.sql}
 * et a l'entite PROMOTION du diagramme D2.
 */
@Entity
@Table(name = "promotion")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    protected Promotion() {
        // requis par JPA
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }
}
