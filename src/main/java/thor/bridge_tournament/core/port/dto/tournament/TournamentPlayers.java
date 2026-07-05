package thor.bridge_tournament.core.port.dto.tournament;

import thor.bridge_tournament.core.port.dto.UserDto;

import java.util.List;

public record TournamentPlayers(List<UserDto> playersWithOutPair, List<PairDto> pairs) {
}
