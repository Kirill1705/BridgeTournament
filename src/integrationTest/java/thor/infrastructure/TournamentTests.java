package thor.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
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
import thor.bridge_tournament.core.port.dto.UserDto;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = thor.bridge_tournament.BridgeTournamentApplication.class)
@Testcontainers
@ActiveProfiles("test")
@ComponentScan(basePackages = {"thor.bridge_tournament.core.service", "thor.bridge_tournament.infrastructure"})
class TournamentTests {
    @Autowired private TournamentService tournaments;
    @Autowired private UserService users;
    private final Map<String, UUID> userIds = new HashMap<>();
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
        tournaments.createTournament(user("owner"), 8, "IMP", "Test", 4);
        var tournamentId = current.getByTd(user("owner")).getUuid();
        for (int i = 1; i <= 6; i++) {
            tournaments.addPair(user("owner"), user("p" + i + "a"), user("p" + i + "b"));
        }
        movements.addMovement(readHowell6());
        boards.addBoard(20); // Board database IDs must not be confused with board numbers.
        tournaments.startTournament(user("owner"));
        entityManager.flush();
        entityManager.clear();
        assertTrue(current.getByTd(user("owner")).isStarted());
        assertEquals(IntStream.rangeClosed(1, 8).boxed().toList(), boardRepository.getBoardsForTournament(tournamentId)
                .stream().map(board -> board.number()).sorted().toList());
        assertEquals(4, movements.getMovementCard(user("p1a")).size());
        var first = movements.getMovementNextRound(user("p1a"));
        assertEquals(1, first.movementEntry().round());
        assertEquals(2, first.dealsNotPlayed().size());
        var firstBoard = first.dealsNotPlayed().getFirst();
        var secondBoard = first.dealsNotPlayed().getLast();
        var playerId = user("p1a");
        var firstNode = nodes.findNode(playerId, tournamentId, firstBoard).orElseThrow();
        // A result for the same board at another table must not complete this meeting.
        for (int i = 2; i <= 6; i++) {
            String otherPlayer = "p" + i + "a";
            var otherNode = nodes.findNode(user(otherPlayer), tournamentId, firstBoard).orElseThrow();
            if (!otherNode.id().equals(firstNode.id())) {
                entries.addTournamentBoardEntry(user(otherPlayer), firstBoard, new RawBoardEntry("4S", "N", "cK", 1));
                break;
            }
        }
        assertEquals(first.dealsNotPlayed(), nodes.getDealsNotPlayed(firstNode.id()));
        entries.addTournamentBoardEntry(user("p1a"), firstBoard, new RawBoardEntry("4S", "N", "cK", 0));
        var partial = movements.getMovementNextRound(user("p1a"));
        assertEquals(1, partial.movementEntry().round());
        assertEquals(List.of(secondBoard), partial.dealsNotPlayed());
        entries.addTournamentBoardEntry(user("p1a"), secondBoard, new RawBoardEntry("3NT", "N", "cK", 0));
        assertEquals(2, movements.getMovementNextRound(user("p1a")).movementEntry().round());
        assertTrue(nodes.getDealsNotPlayed(firstNode.id()).isEmpty());
        for (int round = 2; round <= 4; round++) {
            var next = movements.getMovementNextRound(user("p1a"));
            assertEquals(round, next.movementEntry().round());
            for (int board : next.dealsNotPlayed()) {
                entries.addTournamentBoardEntry(user("p1a"), board, new RawBoardEntry("pass", null, null, 0));
            }
        }
        assertTrue(nodes.findNextNodeForPlayer(playerId, tournamentId).isEmpty());
    }

    @Test
    void startsWithAnOddNumberOfUnpairedPlayers() {
        tournaments.createTournament(user("owner"), 2, "IMP", "Odd players", 1);
        for (int i = 1; i <= 5; i++) {
            tournaments.addPlayer(user("owner"), user("p" + i));
            assertEquals(current.getByTd(user("owner")).getUuid(), current.getByPlayerId(user("p" + i)).getUuid());
        }
        movements.addMovement(new MovementDto("howell", 1, 2, List.of(new MovementBodyNode(1, 2, 1, 1, 0))));
        tournaments.startTournament(user("owner"));
        assertTrue(current.getByTd(user("owner")).isStarted());
        assertEquals(2, tournaments.getAllPlayers(user("owner")).pairs().size());
    }

    @Test
    void switchingPlayerTournamentPreservesDirectorTournamentAndViceVersa() {
        tournaments.createTournament(user("owner"), 2, "IMP", "First", 1);
        UUID first = current.getByTd(user("owner")).getUuid();
        tournaments.addPair(user("owner"), user("owner"), user("partner"));
        assertEquals(first, current.getByTd(user("owner")).getUuid());
        assertEquals(first, current.getByPlayerId(user("owner")).getUuid());
        assertEquals(first, current.getByPlayerId(user("partner")).getUuid());
        tournaments.createTournament(user("owner"), 2, "IMP", "Second", 1);
        UUID second = current.getByTd(user("owner")).getUuid();
        assertNotEquals(first, second);
        assertEquals(first, current.getByPlayerId(user("owner")).getUuid());
        currentRepository.switchPlayer(second, user("owner"));
        assertEquals(second, current.getByPlayerId(user("owner")).getUuid());
        assertEquals(second, current.getByTd(user("owner")).getUuid());
    }

    private UUID user(String label) {
        return userIds.computeIfAbsent(label, name -> users.register(new UserDto(null, name, null, null, 5.0)));
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
