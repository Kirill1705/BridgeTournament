package thor.bridge_tournament.core.port.output.repository;

import thor.bridge_tournament.core.port.dto.board.BoardDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardRepository {
    int save(BoardDto board);

    void addBoardToTournament(int boardId, UUID tournamentId);

    List<BoardDto> getBoardsForTournament(UUID tournamentId);

    Optional<BoardDto> getById(int id);

    Optional<BoardDto> findByTournament(UUID tournamentId, int boardNumber);
}
