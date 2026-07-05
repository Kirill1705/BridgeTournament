package thor.bridge_tournament.core.port.dto.tournament_result;

import thor.bridge_tournament.core.port.dto.tournament.PairDto;

public record PairTournamentResultDto(int rank, PairDto pair, double points, String countType) {
}
