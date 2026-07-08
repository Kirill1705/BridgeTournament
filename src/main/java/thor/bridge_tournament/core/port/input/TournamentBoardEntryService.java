package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;

public interface TournamentBoardEntryService {
    PairBoardResult addTournamentBoardEntry(String userName, int boardNumber, RawBoardEntry entry);
}
