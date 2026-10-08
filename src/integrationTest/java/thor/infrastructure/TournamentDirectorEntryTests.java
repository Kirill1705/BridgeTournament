package thor.infrastructure;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import thor.bridge_tournament.BridgeTournamentApplication;
import thor.bridge_tournament.core.domain.CurrentTournamentManager;
import thor.bridge_tournament.core.domain.tournament.TournamentNode;
import thor.bridge_tournament.core.exception.DomainValidationException;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.dto.tournament.TournamentDto;
import thor.bridge_tournament.core.port.input.BoardEntryService;
import thor.bridge_tournament.core.port.input.BoardService;
import thor.bridge_tournament.core.port.input.TournamentBoardEntryService;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.core.port.output.repository.BoardEntryRepository;
import thor.bridge_tournament.core.port.output.repository.TournamentNodeRepository;
import thor.bridge_tournament.core.port.output.repository.TournamentRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.datasource.url=jdbc:tc:postgresql:15-alpine:///director_entries")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = BridgeTournamentApplication.class)
@ActiveProfiles("test")
@ComponentScan(basePackages = {"thor.bridge_tournament.core.service", "thor.bridge_tournament.infrastructure"})
class TournamentDirectorEntryTests {
    @Autowired private UserService users;
    @Autowired private TournamentService tournaments;
    @Autowired private TournamentBoardEntryService service;
    @Autowired private BoardEntryService standalone;
    @Autowired private BoardService boards;
    @Autowired private CurrentTournamentManager current;
    @Autowired private TournamentNodeRepository nodes;
    @Autowired private TournamentRepository tournamentRepository;
    @Autowired private BoardEntryRepository entries;
    @Autowired private EntityManager entityManager;

    @ParameterizedTest
    @ValueSource(strings = {"IMP", "MP"})
    void directorCanCorrectPlayerAndWriteSameBoardElsewhereWithoutCollisions(String countType) {
        UUID td = users.register(new UserDto(null, "director", null, null, 5d));
        tournaments.createTournament(td, 2, countType, "Director entries", 1);
        UUID tournamentId = current.getByTd(td).getUuid();
        var players = IntStream.rangeClosed(1, 8)
                .mapToObj(i -> users.register(new UserDto(null, "p" + i, null, null, 5d))).toList();
        for (int i = 0; i < 8; i += 2) {
            tournaments.addPair(td, players.get(i), players.get(i + 1));
        }
        var pairs = tournaments.getAllPlayers(td).pairs();
        PairDto ns = pairs.get(0), ew = pairs.get(1), otherNs = pairs.get(2), otherEw = pairs.get(3);
        nodes.addNodes(List.of(
                new TournamentNode(null, tournamentId, 1, 0, List.of(1), ns, ew),
                new TournamentNode(null, tournamentId, 1, 1, List.of(1), otherNs, otherEw)));
        int boardId = boards.addBoard(1);
        tournaments.addBoardToTournament(boardId, td);
        var tournament = tournamentRepository.getById(tournamentId);
        tournamentRepository.save(new TournamentDto(tournamentId, td, tournament.tds(), tournament.countType(), 2, 1, tournament.name(), true));
        var original = new RawBoardEntry("4S", "N", "CK", 0);
        UUID standaloneId = standalone.addBoardEntryImps(td, boardId, original, 0d).entryId();
        var playerEntry = service.addTournamentBoardEntryForPlayer(ns.firstPlayer().id(), 1, original);
        var corrected = service.addTournamentBoardEntryByTd(td, 1, new RawBoardEntry("3NT", "W", "H2", 1),
                ew.secondPlayer().id(), ns.secondPlayer().id());
        var other = service.addTournamentBoardEntryByTd(td, 1, new RawBoardEntry("pass", null, null, 0),
                otherNs.firstPlayer().id(), otherEw.firstPlayer().id());
        entityManager.flush();
        entityManager.clear();

        assertEquals(playerEntry.entryId(), corrected.entryId());
        assertNotEquals(corrected.entryId(), other.entryId());
        assertEquals(2, entries.findByTournamentId(tournamentId).size());
        assertEquals(3, entries.findByBoardId(boardId).size());
        assertEquals(standaloneId, entries.getEntryId(td, boardId).orElseThrow());
        assertEquals(2, other.protocol().size());
        var node = nodes.findNode(ns.firstPlayer().id(), tournamentId, 1).orElseThrow();
        var stored = entries.findByMeeting(node.id(), boardId).orElseThrow();
        assertEquals(td, stored.writer().id());
        assertEquals("3NT", stored.contract());
        assertEquals(ns, stored.ns());
        assertEquals(ew, stored.ew());
        assertTrue(nodes.getDealsNotPlayed(node.id()).isEmpty());
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryForPlayer(ns.firstPlayer().id(), 1, original));
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryByTd(td, 1, original,
                ns.firstPlayer().id(), otherEw.firstPlayer().id()));
    }
}
