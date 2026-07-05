package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.infrastructure.jpa.BoardEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardJpaRepository extends JpaRepository<BoardEntity, Integer> {
    @Query(value = "SELECT b.* FROM boards b JOIN tournament_boards tb ON tb.board_id = b.id WHERE tb.tournament_id = :tournamentId", nativeQuery = true)
    List<BoardEntity> getAllTournamentBoards(UUID tournamentId);

    @Query(value = "SELECT b.* FROM boards b JOIN tournament_boards tb ON tb.board_id = b.id WHERE tb.tournament_id = :tournamentId AND b.number = :number", nativeQuery = true)
    Optional<BoardEntity> findTournamentBoard(UUID tournamentId, int number);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tournament_boards(tournament_id, board_id) VALUES (:tournamentId, :boardId)", nativeQuery = true)
    void addBoardToTournament(UUID tournamentId, int boardId);

}
