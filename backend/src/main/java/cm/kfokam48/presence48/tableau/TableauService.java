package cm.kfokam48.presence48.tableau;

import cm.kfokam48.presence48.erreur.CodeErreur;
import cm.kfokam48.presence48.erreur.ExceptionMetier;
import cm.kfokam48.presence48.referentiel.PromotionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Le tableau recapitulatif du formateur (EF11, Q16). */
@Service
@Transactional(readOnly = true)
public class TableauService {

    private final TableauRepository tableau;
    private final PromotionRepository promotions;

    public TableauService(TableauRepository tableau, PromotionRepository promotions) {
        this.tableau = tableau;
        this.promotions = promotions;
    }

    /**
     * @throws ExceptionMetier {@code 404 PROMOTION_INCONNUE}. Une promotion sans
     *     etudiant renvoie une liste vide ; une promotion inexistante, une
     *     erreur. Les deux cas ne se confondent pas.
     */
    public List<LigneTableauDto> pourPromotion(Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw new ExceptionMetier(CodeErreur.PROMOTION_INCONNUE);
        }
        return tableau.tableauDeLaPromotion(promotionId);
    }
}
