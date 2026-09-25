package cm.kfokam48.presence48.session;

import java.time.OffsetDateTime;

/**
 * Une session telle que l'API l'expose sur les operations ajoutees — schema
 * Session du contrat. {@code cloturee} est calcule et non stocke : une session
 * est ouverte si {@code clotureeAt} est nul, ce qui evite deux sources de verite.
 */
public record SessionDto(
        Long id,
        String titre,
        Long promotionId,
        String code,
        OffsetDateTime ouvertureAt,
        OffsetDateTime expirationAt,
        OffsetDateTime clotureeAt,
        boolean cloturee) {
}
