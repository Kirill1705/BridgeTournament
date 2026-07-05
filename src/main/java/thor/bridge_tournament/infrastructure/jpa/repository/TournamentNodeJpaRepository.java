package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.infrastructure.jpa.TournamentNodeEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentNodeJpaRepository extends JpaRepository<TournamentNodeEntity, UUID> {
    @Query("""
        SELECT DISTINCT tn FROM TournamentNodeEntity tn
        LEFT JOIN FETCH tn.ns
        LEFT JOIN FETCH tn.ew
        LEFT JOIN tn.ns pns
        LEFT JOIN tn.ew pew
        WHERE (pns.firstPlayer.id = :playerId OR pns.secondPlayer.id = :playerId OR pew.firstPlayer.id = :playerId OR pew.secondPlayer.id = :playerId) AND tn.tournamentId = :tournamentId
        ORDER BY tn.round
        """)
    List<TournamentNodeEntity> findByPlayersAndTournament(UUID playerId, UUID tournamentId);

    @Query("""
        SELECT tn FROM TournamentNodeEntity tn
        LEFT JOIN FETCH tn.ns
        LEFT JOIN FETCH tn.ew
        LEFT JOIN tn.ns pns
        LEFT JOIN tn.ew pew
        JOIN tn.boards tnb
        WHERE (pns.firstPlayer.id = :playerId OR pns.secondPlayer.id = :playerId OR pew.firstPlayer.id = :playerId OR pew.secondPlayer.id = :playerId) AND tn.tournamentId = :tournamentId AND tnb = :boardNumber
        """)
    Optional<TournamentNodeEntity> findByPlayerTournamentBoardNumber(UUID playerId, UUID tournamentId, int boardNumber);

    @Query("""
        SELECT tnb FROM TournamentNodeEntity tn
        JOIN tn.boards tnb
        LEFT JOIN TournamentNodeEntryEntity tne ON tne.id.nodeId = tn.id
        WHERE tn.id = :nodeId AND tne.id.boardEntryId IS NULL
        ORDER BY tnb
        """)
    List<Integer> findNotPlayedDeals(UUID nodeId);

    @Query("""
        SELECT tn FROM TournamentNodeEntity tn
        LEFT JOIN FETCH tn.ns
        LEFT JOIN FETCH tn.ew
        LEFT JOIN tn.ns pns
        LEFT JOIN tn.ew pew
        WHERE (pns.firstPlayer.id = :playerId OR pns.secondPlayer.id = :playerId OR pew.firstPlayer.id = :playerId OR pew.secondPlayer.id = :playerId) AND tn.tournamentId = :tournamentId AND tn.id NOT IN (
            SELECT tne.id.nodeId FROM TournamentNodeEntryEntity tne
            WHERE tne.id.boardEntryId IS NOT NULL
        )
        ORDER BY tn.round
        """)
    List<TournamentNodeEntity> findByPlayersTournamentAndNotPlayed(UUID playerId, UUID tournamentId, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tournament_node_entries(node_id, board_entry_id) VALUES (:nodeId, :boardEntryId)", nativeQuery = true)
    void addEntry(UUID nodeId, UUID boardEntryId);
}
