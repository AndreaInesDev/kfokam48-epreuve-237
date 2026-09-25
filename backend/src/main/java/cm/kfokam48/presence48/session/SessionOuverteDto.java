package cm.kfokam48.presence48.session;

import java.time.OffsetDateTime;

/**
 * Reponse 201 de {@code POST /api/sessions}, imposee par le contrat :
 * exactement {@code { id, code, ouvertureAt, expirationAt }}.
 */
public record SessionOuverteDto(
        Long id,
        String code,
        OffsetDateTime ouvertureAt,
        OffsetDateTime expirationAt) {
}
