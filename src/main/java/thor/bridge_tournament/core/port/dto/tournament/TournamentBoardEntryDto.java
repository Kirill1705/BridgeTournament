package thor.bridge_tournament.core.port.dto.tournament;

import java.util.UUID;

public record TournamentBoardEntryDto(UUID boardEntryId, UUID tournamentNodeId) {
}
