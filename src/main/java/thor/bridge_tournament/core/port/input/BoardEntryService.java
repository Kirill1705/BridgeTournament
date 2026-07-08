package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;

public interface BoardEntryService {
    PairBoardResult addBoardEntryImps(String username, int boardId, RawBoardEntry entry, Double throwAwayPercent);

    PairBoardResult addBoardEntryMp(String username, int boardId, RawBoardEntry entry);
}
