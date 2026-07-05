package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.dto.movement.MovementDto;
import thor.bridge_tournament.core.port.dto.movement.PairMovementEntryDto;
import thor.bridge_tournament.core.port.dto.movement.PairMovementNextRoundInfo;

import java.util.List;

public interface MovementService {
    PairBoardResult addTournamentBoardEntry(String userName, int boardNumber, RawBoardEntry entry);

    PairBoardResult addBoardEntryImps(String username, int boardId, RawBoardEntry entry, Double throwAwayPercent);

    PairBoardResult addBoardEntryMp(String username, int boardId, RawBoardEntry entry);

    List<PairMovementEntryDto> getMovementCard(String userName);

    PairMovementNextRoundInfo getMovementNextRound(String userName);

    void addMovement(MovementDto movementDto);
}
