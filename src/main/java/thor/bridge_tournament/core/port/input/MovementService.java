package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.dto.movement.MovementDto;
import thor.bridge_tournament.core.port.dto.movement.PairMovementEntryDto;
import thor.bridge_tournament.core.port.dto.movement.PairMovementNextRoundInfo;

import java.util.List;
import java.util.UUID;

public interface MovementService {
    List<PairMovementEntryDto> getMovementCard(UUID userId);

    PairMovementNextRoundInfo getMovementNextRound(UUID userId);

    void addMovement(MovementDto movementDto);
}
