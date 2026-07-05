package thor.bridge_tournament.core.domain.tournament;

import thor.bridge_tournament.core.port.dto.tournament.PairDto;

import java.util.List;
import java.util.UUID;

public record TournamentNode(UUID id, UUID tournamentId, int round, int table, List<Integer> boards, PairDto ns, PairDto ew) {
}
