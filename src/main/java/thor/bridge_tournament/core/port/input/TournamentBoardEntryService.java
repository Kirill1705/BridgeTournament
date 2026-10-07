package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;

import java.util.UUID;

public interface TournamentBoardEntryService {
    /**
     * @param userId внутренний идентификатор игрока, отправившего результат; не идентификатор судьи или партнёра
     */
    PairBoardResult addTournamentBoardEntry(UUID userId, int boardNumber, RawBoardEntry entry);
}
