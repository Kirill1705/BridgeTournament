package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import thor.bridge_tournament.infrastructure.jpa.BoardEntryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardEntryJpaRepository extends JpaRepository<BoardEntryEntity, UUID> {
    @Query("SELECT be.id FROM BoardEntryEntity be WHERE be.writer.id = :writerId AND be.board.id = :boardId")
    Optional<UUID> findEntryIdByWriterIdAndBoardId(UUID writerId, int boardId);

    @EntityGraph(attributePaths = {"board", "writer", "ns.firstPlayer", "ns.secondPlayer", "ew.firstPlayer", "ew.secondPlayer"})
    List<BoardEntryEntity> findByBoardId(int boardId);

    @Query(value = "SELECT be.id FROM board_entries be JOIN tournament_boards tb ON tb.board_id = be.board_id WHERE tb.tournament_id = :tournamentId AND be.removed = false", nativeQuery = true)
    List<UUID> findIdsByTournamentId(UUID tournamentId);

    @Override
    @EntityGraph(attributePaths = {"board", "writer", "ns.firstPlayer", "ns.secondPlayer", "ew.firstPlayer", "ew.secondPlayer"})
    List<BoardEntryEntity> findAllById(Iterable<UUID> ids);
}
