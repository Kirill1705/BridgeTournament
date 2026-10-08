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
        LEFT JOIN FETCH tn.ns pns
        LEFT JOIN FETCH pns.firstPlayer
        LEFT JOIN FETCH pns.secondPlayer
        LEFT JOIN FETCH tn.ew pew
        LEFT JOIN FETCH pew.firstPlayer
        LEFT JOIN FETCH pew.secondPlayer
        LEFT JOIN FETCH tn.boards
        WHERE (pns.firstPlayer.id = :playerId OR pns.secondPlayer.id = :playerId OR pew.firstPlayer.id = :playerId OR pew.secondPlayer.id = :playerId) AND tn.tournamentId = :tournamentId
        ORDER BY tn.round
        """)
    List<TournamentNodeEntity> findByPlayersAndTournament(UUID playerId, UUID tournamentId);

    @Query("""
        SELECT tn FROM TournamentNodeEntity tn
        LEFT JOIN FETCH tn.ns pns
        LEFT JOIN FETCH pns.firstPlayer
        LEFT JOIN FETCH pns.secondPlayer
        LEFT JOIN FETCH tn.ew pew
        LEFT JOIN FETCH pew.firstPlayer
        LEFT JOIN FETCH pew.secondPlayer
        JOIN tn.boards tnb
        WHERE (pns.firstPlayer.id = :playerId OR pns.secondPlayer.id = :playerId OR pew.firstPlayer.id = :playerId OR pew.secondPlayer.id = :playerId) AND tn.tournamentId = :tournamentId AND tnb = :boardNumber
        """)
    Optional<TournamentNodeEntity> findByPlayerTournamentBoardNumber(UUID playerId, UUID tournamentId, int boardNumber);

    @Query(value = "SELECT id FROM tournament_nodes WHERE id = :nodeId FOR UPDATE", nativeQuery = true)
    UUID lockNode(UUID nodeId);

    @Query("""
        SELECT tnb FROM TournamentNodeEntity tn
        JOIN tn.boards tnb
        WHERE tn.id = :nodeId AND NOT EXISTS (
            SELECT tne.id.boardEntryId FROM TournamentNodeEntryEntity tne
            JOIN BoardEntryEntity be ON be.id = tne.id.boardEntryId
            WHERE tne.id.nodeId = tn.id AND be.board.number = tnb
        )
        ORDER BY tnb
        """)
    List<Integer> findNotPlayedDeals(UUID nodeId);

    @Query("""
        SELECT tn FROM TournamentNodeEntity tn
        LEFT JOIN FETCH tn.ns pns
        LEFT JOIN FETCH pns.firstPlayer
        LEFT JOIN FETCH pns.secondPlayer
        LEFT JOIN FETCH tn.ew pew
        LEFT JOIN FETCH pew.firstPlayer
        LEFT JOIN FETCH pew.secondPlayer
        WHERE (pns.firstPlayer.id = :playerId OR pns.secondPlayer.id = :playerId OR pew.firstPlayer.id = :playerId OR pew.secondPlayer.id = :playerId) AND tn.tournamentId = :tournamentId AND EXISTS (
            SELECT tnb FROM TournamentNodeEntity pendingNode
            JOIN pendingNode.boards tnb
            WHERE pendingNode.id = tn.id AND NOT EXISTS (
                SELECT tne.id.boardEntryId FROM TournamentNodeEntryEntity tne
                JOIN BoardEntryEntity be ON be.id = tne.id.boardEntryId
                WHERE tne.id.nodeId = tn.id AND be.board.number = tnb
            )
        )
        ORDER BY tn.round
        """)
    List<TournamentNodeEntity> findByPlayersTournamentAndNotPlayed(UUID playerId, UUID tournamentId, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tournament_node_entries(node_id, board_entry_id) VALUES (:nodeId, :boardEntryId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void addEntry(UUID nodeId, UUID boardEntryId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tournament_node_entries WHERE node_id = :nodeId AND board_entry_id = :boardEntryId)", nativeQuery = true)
    boolean hasEntry(UUID nodeId, UUID boardEntryId);
}
