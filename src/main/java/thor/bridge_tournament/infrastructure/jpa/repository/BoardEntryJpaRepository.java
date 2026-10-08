package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import thor.bridge_tournament.infrastructure.jpa.BoardEntryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardEntryJpaRepository extends JpaRepository<BoardEntryEntity, UUID> {
    @Query("SELECT be.id FROM BoardEntryEntity be WHERE be.writer.id = :writerId AND be.board.id = :boardId AND be.ns IS NULL AND be.ew IS NULL")
    Optional<UUID> findEntryIdByWriterIdAndBoardId(UUID writerId, int boardId);

    @EntityGraph(attributePaths = {"board", "writer", "ns.firstPlayer", "ns.secondPlayer", "ew.firstPlayer", "ew.secondPlayer"})
    @Query("""
            SELECT be FROM BoardEntryEntity be
            JOIN TournamentNodeEntryEntity tne ON tne.id.boardEntryId = be.id
            WHERE tne.id.nodeId = :nodeId AND be.board.id = :boardId AND be.removed = false
            """)
    Optional<BoardEntryEntity> findByMeeting(UUID nodeId, int boardId);

    @EntityGraph(attributePaths = {"board", "writer", "ns.firstPlayer", "ns.secondPlayer", "ew.firstPlayer", "ew.secondPlayer"})
    List<BoardEntryEntity> findByBoardId(int boardId);

    @Query(value = """
            SELECT DISTINCT be.id FROM board_entries be
            JOIN tournament_node_entries tne ON tne.board_entry_id = be.id
            JOIN tournament_nodes tn ON tn.id = tne.node_id
            WHERE tn.tournament_id = :tournamentId AND be.removed = false
            """, nativeQuery = true)
    List<UUID> findIdsByTournamentId(UUID tournamentId);

    @Override
    @EntityGraph(attributePaths = {"board", "writer", "ns.firstPlayer", "ns.secondPlayer", "ew.firstPlayer", "ew.secondPlayer"})
    List<BoardEntryEntity> findAllById(Iterable<UUID> ids);
}
