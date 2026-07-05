package thor.bridge_tournament.infrastructure.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.core.port.dto.board.BoardDto;
import thor.bridge_tournament.core.port.output.repository.BoardRepository;
import thor.bridge_tournament.infrastructure.jpa.repository.BoardJpaRepository;
import thor.bridge_tournament.infrastructure.mapping.BoardMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {
    private final BoardJpaRepository repository;
    private final BoardMapper mapper;

    @Override
    @Transactional
    public int save(BoardDto board) {
        return repository.save(mapper.toJpa(board)).getId();
    }

    @Override
    @Transactional
    public void addBoardToTournament(int boardId, UUID tournamentId) {
        repository.addBoardToTournament(tournamentId, boardId);
    }

    @Override
    public List<BoardDto> getBoardsForTournament(UUID tournamentId) {
        return repository.getAllTournamentBoards(tournamentId).stream().map(mapper::toDto).toList();
    }

    @Override
    public Optional<BoardDto> getById(int id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public Optional<BoardDto> findByTournament(UUID tournamentId, int boardNumber) {
        return repository.findTournamentBoard(tournamentId, boardNumber).map(mapper::toDto);
    }
}
