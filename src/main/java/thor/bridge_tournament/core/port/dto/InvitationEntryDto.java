package thor.bridge_tournament.core.port.dto;

import java.util.UUID;

public record InvitationEntryDto(
        UUID uuid,
        UUID initiatorId,
        UUID userId,
        UUID tournamentId
) {
}
