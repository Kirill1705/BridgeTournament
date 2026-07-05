package thor.bridge_tournament.core.port.dto.tournament;

import java.util.List;
import java.util.UUID;

public record TournamentDto(
        UUID id,
        UUID ownerId,
        List<UUID> tds,
        String countType,
        int boards,
        Integer rounds,
        String name,
        boolean started
) {
}
