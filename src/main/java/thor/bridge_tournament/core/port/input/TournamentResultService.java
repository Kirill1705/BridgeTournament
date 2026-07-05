package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.tournament_result.PairTournamentResultDto;

import java.util.List;

public interface TournamentResultService {
    List<PairTournamentResultDto> getRanks(String userName);
}
