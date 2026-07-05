package thor.bridge_tournament.infrastructure.repository;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.core.domain.tournament.TournamentNode;
import thor.bridge_tournament.core.port.output.repository.TournamentNodeRepository;
import thor.bridge_tournament.infrastructure.jpa.repository.TournamentNodeJpaRepository;
import thor.bridge_tournament.infrastructure.mapping.TournamentNodeMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class TournamentNodeRepositoryImpl implements TournamentNodeRepository {
    private final TournamentNodeJpaRepository repository;
    private final TournamentNodeMapper mapper;

    @Override
    @Transactional
    public void addNodes(List<TournamentNode> nodes) {
        repository.saveAll(nodes.stream().map(mapper::toJpa).toList());
    }

    @Override
    public List<TournamentNode> getAllMovementsForPlayer(UUID playerId, UUID tournamentId) {
        return repository.findByPlayersAndTournament(playerId, tournamentId).stream().map(mapper::toDto).toList();
    }

    @Override
    public Optional<TournamentNode> findNextNodeForPlayer(UUID playerId, UUID tournamentId) {
        return repository.findByPlayersTournamentAndNotPlayed(playerId, tournamentId, Pageable.ofSize(1)).stream().findAny().map(mapper::toDto);
    }

    @Override
    public Optional<TournamentNode> findNode(UUID playerId, UUID tournamentId, int boardNumber) {
        return repository.findByPlayerTournamentBoardNumber(playerId, tournamentId, boardNumber).map(mapper::toDto);
    }

    @Override
    public List<Integer> getDealsNotPlayed(UUID nodeId) {
        return repository.findNotPlayedDeals(nodeId);
    }

    @Override
    @Transactional
    public void addEntry(UUID nodeId, UUID boardEntryId) {
        repository.addEntry(nodeId, boardEntryId);
    }
}
