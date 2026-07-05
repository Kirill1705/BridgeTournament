package thor.bridge_tournament.core.port.output.repository;

import java.util.Optional;
import java.util.UUID;

public interface CurrentTournamentRepository {
    Optional<UUID> getTournamentIdByTd(UUID tdId);
    Optional<UUID> getTournamentIdByPlayer(UUID playerId);
    void switchTd(UUID tournamentId, UUID tdId);
    void switchPlayer(UUID tournamentId, UUID playerId);
}
