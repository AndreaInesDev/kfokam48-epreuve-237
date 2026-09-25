package cm.kfokam48.presence48.tableau;

import cm.kfokam48.presence48.domaine.Etudiant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Le tableau du formateur, en <b>une seule requete agregee</b>.
 *
 * <p>ENF2 exige une reponse en moins de deux secondes pour une promotion de 60
 * etudiants. Charger les etudiants puis compter leurs presences, leurs exercices
 * et leurs relectures un par un donnerait 4 requetes par etudiant, soit 241
 * allers-retours — le N+1 classique. Ici, tout est calcule par la base.
 */
public interface TableauRepository extends JpaRepository<Etudiant, Long> {

    /**
     * Q16 — par etudiant : sa presence a chaque session, combien d'exercices il
     * a deposes, la moyenne des notes recues, et les relectures qu'il doit
     * encore faire.
     *
     * <p>RG18 : la moyenne ne porte que sur les relectures <b>rendues</b>, et
     * {@code avg} sur un ensemble vide renvoie {@code null} — exactement ce que
     * le contrat declare. RG16 : une relecture en attente est une relecture
     * assignee a cet etudiant dont {@code rendueAt} est nul.
     */
    @Query("""
            select new cm.kfokam48.presence48.tableau.LigneTableauDto(
                e.id,
                e.nom,
                (select count(p) from Presence p where p.etudiant = e),
                (select count(x) from Exercice x where x.etudiant = e),
                (select avg(r.note) from Relecture r
                     where r.exercice.etudiant = e and r.rendueAt is not null),
                (select count(a) from Relecture a
                     where a.relecteur = e and a.rendueAt is null))
            from Etudiant e
            where e.promotion.id = :promotionId
            order by e.nom asc
            """)
    List<LigneTableauDto> tableauDeLaPromotion(@Param("promotionId") Long promotionId);
}
