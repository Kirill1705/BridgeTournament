package thor.bridge_tournament.core.port.output.repository;

import thor.bridge_tournament.core.port.dto.board.BoardEntryDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardEntryRepository {
    Optional<UUID> getEntryId(UUID writerId, int boardId);

    UUID save(BoardEntryDto boardEntry);

    List<BoardEntryDto> findByBoardId(int boardId);

    List<BoardEntryDto> findByTournamentId(UUID tournamentId);
}
