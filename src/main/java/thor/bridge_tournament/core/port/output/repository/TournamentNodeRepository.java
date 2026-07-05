package thor.bridge_tournament.core.port.output.repository;

import thor.bridge_tournament.core.domain.tournament.TournamentNode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentNodeRepository {
    void addNodes(List<TournamentNode> nodes);

    List<TournamentNode> getAllMovementsForPlayer(UUID playerId, UUID tournamentId);

    Optional<TournamentNode> findNextNodeForPlayer(UUID playerId, UUID tournamentId);

    Optional<TournamentNode> findNode(UUID playerId, UUID tournamentId, int boardNumber);

    List<Integer> getDealsNotPlayed(UUID nodeId);

    void addEntry(UUID nodeId, UUID boardEntryId);
}
