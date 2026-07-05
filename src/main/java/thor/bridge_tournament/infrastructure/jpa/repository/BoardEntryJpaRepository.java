package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import thor.bridge_tournament.infrastructure.jpa.BoardEntryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardEntryJpaRepository extends JpaRepository<BoardEntryEntity, UUID> {
    Optional<BoardEntryEntity> findByWriterIdAndBoardId(UUID writerId, int boardId);

    List<BoardEntryEntity> findByBoardId(int boardId);

    @Query(value = "SELECT be.* FROM board_entries be JOIN tournament_boards tb ON tb.board_id = be.board_id WHERE tb.tournament_id = :tournamentId", nativeQuery = true)
    List<BoardEntryEntity> findByTournamentId(UUID tournamentId);
}
