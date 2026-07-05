package thor.bridge_tournament.core.port.dto.tournament;

import thor.bridge_tournament.core.port.dto.UserDto;

import java.util.UUID;

public record PairDto(UUID id, UserDto firstPlayer, UserDto secondPlayer) {
}
