package thor.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import thor.bridge_tournament.core.domain.CurrentTournamentManager;
import thor.bridge_tournament.core.domain.movement.MovementBodyNode;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.dto.movement.MovementDto;
import thor.bridge_tournament.core.port.input.*;
import thor.bridge_tournament.core.port.output.repository.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = thor.bridge_tournament.BridgeTournamentApplication.class)
@Testcontainers
@ActiveProfiles("test")
@ComponentScan(basePackages = {"thor.bridge_tournament.core.service", "thor.bridge_tournament.infrastructure"})
class TournamentTests {
    @Autowired private TournamentService tournaments;
    @Autowired private BoardService boards;
    @Autowired private MovementService movements;
    @Autowired private TournamentBoardEntryService entries;
    @Autowired private CurrentTournamentManager current;
    @Autowired private CurrentTournamentRepository currentRepository;
    @Autowired private BoardRepository boardRepository;
    @Autowired private TournamentNodeRepository nodes;
    @Autowired private EntityManager entityManager;

    @Test
    void startsTournamentAndAdvancesOnlyAfterAllBoardsOfTheMeetingAreRecorded() throws Exception {
        tournaments.createTournament("owner", 8, "IMP", "Test", 4);
        var tournamentId = current.getByTd("owner").getUuid();
        for (int i = 1; i <= 6; i++) {
            tournaments.addPair("owner", "p" + i + "a", "p" + i + "b");
        }
        movements.addMovement(readHowell6());
        boards.addBoard(20); // Board database IDs must not be confused with board numbers.
        tournaments.startTournament("owner");
        entityManager.flush();
        entityManager.clear();
        assertTrue(current.getByTd("owner").isStarted());
        assertEquals(IntStream.rangeClosed(1, 8).boxed().toList(), boardRepository.getBoardsForTournament(tournamentId)
                .stream().map(board -> board.number()).sorted().toList());
        assertEquals(4, movements.getMovementCard("p1a").size());
        var first = movements.getMovementNextRound("p1a");
        assertEquals(1, first.movementEntry().round());
        assertEquals(2, first.dealsNotPlayed().size());
        var firstBoard = first.dealsNotPlayed().getFirst();
        var secondBoard = first.dealsNotPlayed().getLast();
        var playerId = current.getUserIdOrRegister("p1a");
        var firstNode = nodes.findNode(playerId, tournamentId, firstBoard).orElseThrow();
        // A result for the same board at another table must not complete this meeting.
        for (int i = 2; i <= 6; i++) {
            String otherPlayer = "p" + i + "a";
            var otherNode = nodes.findNode(current.getUserIdOrRegister(otherPlayer), tournamentId, firstBoard).orElseThrow();
            if (!otherNode.id().equals(firstNode.id())) {
                entries.addTournamentBoardEntry(otherPlayer, firstBoard, new RawBoardEntry("4S", "N", "cK", 1));
                break;
            }
        }
        assertEquals(first.dealsNotPlayed(), nodes.getDealsNotPlayed(firstNode.id()));
        entries.addTournamentBoardEntry("p1a", firstBoard, new RawBoardEntry("4S", "N", "cK", 0));
        var partial = movements.getMovementNextRound("p1a");
        assertEquals(1, partial.movementEntry().round());
        assertEquals(List.of(secondBoard), partial.dealsNotPlayed());
        entries.addTournamentBoardEntry("p1a", secondBoard, new RawBoardEntry("3NT", "N", "cK", 0));
        assertEquals(2, movements.getMovementNextRound("p1a").movementEntry().round());
        assertTrue(nodes.getDealsNotPlayed(firstNode.id()).isEmpty());
        for (int round = 2; round <= 4; round++) {
            var next = movements.getMovementNextRound("p1a");
            assertEquals(round, next.movementEntry().round());
            for (int board : next.dealsNotPlayed()) {
                entries.addTournamentBoardEntry("p1a", board, new RawBoardEntry("pass", null, null, 0));
            }
        }
        assertTrue(nodes.findNextNodeForPlayer(playerId, tournamentId).isEmpty());
    }

    @Test
    void startsWithAnOddNumberOfUnpairedPlayers() {
        tournaments.createTournament("owner", 2, "IMP", "Odd players", 1);
        for (int i = 1; i <= 5; i++) {
            tournaments.addPlayer("owner", "p" + i);
            assertEquals(current.getByTd("owner").getUuid(), current.getByPlayerId("p" + i).getUuid());
        }
        movements.addMovement(new MovementDto("howell", 1, 2, List.of(new MovementBodyNode(1, 2, 1, 1, 0))));
        tournaments.startTournament("owner");
        assertTrue(current.getByTd("owner").isStarted());
        assertEquals(2, tournaments.getAllPlayers("owner").pairs().size());
    }

    @Test
    void switchingPlayerTournamentPreservesDirectorTournamentAndViceVersa() {
        tournaments.createTournament("owner", 2, "IMP", "First", 1);
        UUID first = current.getByTd("owner").getUuid();
        tournaments.addPair("owner", "owner", "partner");
        assertEquals(first, current.getByTd("owner").getUuid());
        assertEquals(first, current.getByPlayerId("owner").getUuid());
        assertEquals(first, current.getByPlayerId("partner").getUuid());
        tournaments.createTournament("owner", 2, "IMP", "Second", 1);
        UUID second = current.getByTd("owner").getUuid();
        assertNotEquals(first, second);
        assertEquals(first, current.getByPlayerId("owner").getUuid());
        currentRepository.switchPlayer(second, current.getUserIdOrRegister("owner"));
        assertEquals(second, current.getByPlayerId("owner").getUuid());
        assertEquals(second, current.getByTd("owner").getUuid());
    }

    private MovementDto readHowell6() throws Exception {
        var json = new ObjectMapper().readTree(Path.of("movements", "howell6.json").toFile());
        List<MovementBodyNode> body = new ArrayList<>();
        for (int i = 0; i < json.get("body").size(); i++) {
            var row = json.get("body").get(i);
            body.add(new MovementBodyNode(row.get(0).asInt(), row.get(1).asInt(), row.get(2).asInt(), i / 3 + 1, i % 3));
        }
        return new MovementDto("howell", 4, 6, body);
    }
}
