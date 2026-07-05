package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.infrastructure.jpa.TournamentEntity;

import java.util.List;
import java.util.UUID;

public interface TournamentJpaRepository extends JpaRepository<TournamentEntity, UUID> {
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tournament_players(tournament_id, player_id) VALUES (:tournamentId, :playerId)", nativeQuery = true)
    void addPlayerToTournament(UUID tournamentId, UUID playerId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM tournament_players WHERE tournament_id = :tournamentId", nativeQuery = true)
    void removeAllPlayers(UUID tournamentId);

    @Query(value = "SELECT player_id FROM tournament_players WHERE tournament_id = :tournamentId", nativeQuery = true)
    List<UUID> getAllPlayers(UUID tournamentId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tournament_pairs(tournament_id, pair_id) VALUES (:tournamentId, :pairId)", nativeQuery = true)
    void addPairToTournament(UUID tournamentId, UUID pairId);

    @Query(value = "SELECT p.id FROM pairs p JOIN tournament_pairs tp ON p.id = tp.pair_id WHERE tp.tournament_id = :tournamentId", nativeQuery = true)
    List<UUID> getAllPairs(UUID tournamentId);
}
