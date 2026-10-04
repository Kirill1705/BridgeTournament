package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;

import java.util.UUID;

public interface BoardEntryService {
    PairBoardResult addBoardEntryImps(UUID userId, int boardId, RawBoardEntry entry, Double throwAwayPercent);

    PairBoardResult addBoardEntryMp(UUID userId, int boardId, RawBoardEntry entry);
}
