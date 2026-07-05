package thor.bridge_tournament.infrastructure.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.core.port.dto.board.BoardEntryDto;
import thor.bridge_tournament.core.port.output.repository.BoardEntryRepository;
import thor.bridge_tournament.infrastructure.jpa.BoardEntryEntity;
import thor.bridge_tournament.infrastructure.jpa.repository.BoardEntryJpaRepository;
import thor.bridge_tournament.infrastructure.mapping.BoardEntryMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class BoardEntryRepositoryImpl implements BoardEntryRepository {
    private final BoardEntryJpaRepository repository;
    private final BoardEntryMapper mapper;

    @Override
    public Optional<UUID> getEntryId(UUID writerId, int boardId) {
        return repository.findByWriterIdAndBoardId(writerId, boardId).map(BoardEntryEntity::getId);
    }

    @Override
    @Transactional
    public UUID save(BoardEntryDto boardEntry) {
        return repository.save(mapper.toJpa(boardEntry)).getId();
    }

    @Override
    public List<BoardEntryDto> findByBoardId(int boardId) {
        return repository.findByBoardId(boardId).stream().map(mapper::toDto).toList();
    }

    @Override
    public List<BoardEntryDto> findByTournamentId(UUID tournamentId) {
        return repository.findByTournamentId(tournamentId).stream().map(mapper::toDto).toList();
    }
}
