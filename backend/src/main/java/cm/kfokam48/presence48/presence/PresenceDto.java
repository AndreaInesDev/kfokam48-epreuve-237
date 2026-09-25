package cm.kfokam48.presence48.presence;

import cm.kfokam48.presence48.domaine.SourcePresence;

/**
 * Reponse 201 de {@code POST /api/presences}, imposee par le contrat :
 * exactement {@code { id, sessionId, etudiantId, source }}.
 */
public record PresenceDto(Long id, Long sessionId, Long etudiantId, SourcePresence source) {
}
