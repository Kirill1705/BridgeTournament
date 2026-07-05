package thor.bridge_tournament.core.port.dto.board;

import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;

import java.util.UUID;

public record BoardEntryDto(
        UUID id,
        BoardDto board,
        PairDto ns,
        PairDto ew,
        UserDto writer,
        String contract,
        String declarer,
        String lead,
        int result,
        int points
) {
}
