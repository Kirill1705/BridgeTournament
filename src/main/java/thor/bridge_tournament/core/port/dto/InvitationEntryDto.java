package thor.bridge_tournament.core.port.dto;

import java.util.UUID;

public record InvitationEntryDto(
        UUID uuid,
        String initiatorUserName,
        String userName,
        UUID tournamentId
) {
}
