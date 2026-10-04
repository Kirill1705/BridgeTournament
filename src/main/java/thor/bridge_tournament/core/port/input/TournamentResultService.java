package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.tournament_result.PairTournamentResultDto;

import java.util.List;
import java.util.UUID;

public interface TournamentResultService {
    List<PairTournamentResultDto> getRanks(UUID userId);
}
