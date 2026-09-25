package cm.kfokam48.presence48.referentiel;

import cm.kfokam48.presence48.erreur.CodeErreur;
import cm.kfokam48.presence48.erreur.ExceptionMetier;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lecture du referentiel : promotions et etudiants (EF2).
 *
 * <p>Sans ces deux operations, Q1 — « l'etudiant choisit son nom dans une
 * liste » — serait inapplicable : aucune des cinq operations imposees ne fournit
 * cette liste. C'est l'une des zones d'ombre tranchees en section 7.
 */
@Service
@Transactional(readOnly = true)
public class ReferentielService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;

    public ReferentielService(PromotionRepository promotions, EtudiantRepository etudiants) {
        this.promotions = promotions;
        this.etudiants = etudiants;
    }

    public List<PromotionDto> listerPromotions() {
        return promotions.findAllByOrderByNomAsc().stream()
                .map(p -> new PromotionDto(p.getId(), p.getNom()))
                .toList();
    }

    /**
     * Les etudiants d'une promotion, par ordre alphabetique.
     *
     * @throws ExceptionMetier {@code 404 PROMOTION_INCONNUE} si la promotion
     *     n'existe pas. Une promotion vide et une promotion inexistante ne
     *     doivent pas se confondre : la premiere renvoie une liste vide, la
     *     seconde une erreur.
     */
    public List<EtudiantDto> listerEtudiants(Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw new ExceptionMetier(CodeErreur.PROMOTION_INCONNUE);
        }
        return etudiants.findByPromotionIdOrderByNomAsc(promotionId).stream()
                .map(e -> new EtudiantDto(e.getId(), e.getNom()))
                .toList();
    }
}
