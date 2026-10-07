package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.infrastructure.jpa.CurrentTournamentEntity;

import java.util.UUID;
import java.util.List;

public interface CurrentTournamentJpaRepository extends JpaRepository<CurrentTournamentEntity, UUID> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query("UPDATE CurrentTournamentEntity ct SET ct.playerId = null WHERE ct.playerId = :tournamentId AND ct.id IN (:playerIds)")
    void clearPlayers(UUID tournamentId, List<UUID> playerIds);
}
