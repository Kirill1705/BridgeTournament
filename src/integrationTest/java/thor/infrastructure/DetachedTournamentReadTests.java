package thor.infrastructure;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import thor.bridge_tournament.BridgeTournamentApplication;
import thor.bridge_tournament.core.domain.CurrentTournamentManager;
import thor.bridge_tournament.core.domain.tournament.TournamentNode;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.board.BoardEntryDto;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.input.BoardService;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.core.port.output.repository.BoardEntryRepository;
import thor.bridge_tournament.core.port.output.repository.BoardRepository;
import thor.bridge_tournament.core.port.output.repository.PairRepository;
import thor.bridge_tournament.core.port.output.repository.TournamentNodeRepository;
import thor.bridge_tournament.core.port.output.repository.TournamentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:15-alpine:///detached_tournament_reads",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.properties.hibernate.query.fail_on_pagination_over_collection_fetch=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = BridgeTournamentApplication.class)
@ActiveProfiles("test")
@ComponentScan(basePackages = {"thor.bridge_tournament.core.service", "thor.bridge_tournament.infrastructure"})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DetachedTournamentReadTests {
    @Autowired private TournamentService tournaments;
    @Autowired private UserService users;
    @Autowired private CurrentTournamentManager current;
    @Autowired private TournamentRepository tournamentRepository;
    @Autowired private TournamentNodeRepository nodes;
    @Autowired private PairRepository pairs;
    @Autowired private BoardService boards;
    @Autowired private BoardRepository boardRepository;
    @Autowired private BoardEntryRepository entries;
    @Autowired private PlatformTransactionManager transactions;
    @Autowired private EntityManagerFactory entityManagerFactory;

    private Fixture fixture;
    private Statistics statistics;

    @BeforeAll
    void createCommittedFixture() {
        // Only setup has an outer transaction. Reads must work as in a Telegram update handler.
        // The dedicated Testcontainers database is discarded with this test context.
        fixture = new TransactionTemplate(transactions).execute(status -> {
            UUID owner = users.register(new UserDto(null, "owner", "Анна", "Орлова", 1.0));
            UUID director = users.register(new UserDto(null, null, "Борис", "Иванов", 2.0));
            tournaments.createTournament(owner, 4, "IMP", "Detached reads", 2);
            tournaments.addTournamentDirector(owner, director);
            UUID tournamentId = current.getByTd(owner).getUuid();
            var players = IntStream.rangeClosed(1, 4)
                    .mapToObj(i -> users.register(new UserDto(null, "player" + i, "Игрок " + i, "Фамилия", 3.0)))
                    .toList();
            tournaments.addPair(owner, players.get(0), players.get(1));
            tournaments.addPair(owner, players.get(2), players.get(3));
            var pairs = tournaments.getAllPlayers(owner).pairs();
            PairDto ns = pairs.get(0);
            PairDto ew = pairs.get(1);
            nodes.addNodes(List.of(
                    new TournamentNode(null, tournamentId, 1, 1, List.of(1, 2), ns, ew),
                    new TournamentNode(null, tournamentId, 2, 1, List.of(3, 4), ew, ns)));
            var boardIds = new ArrayList<Integer>();
            var entryIds = new ArrayList<UUID>();
            for (int number = 1; number <= 2; number++) {
                int boardId = boards.addBoard(number);
                boardIds.add(boardId);
                tournaments.addBoardToTournament(boardId, owner);
                var board = boardRepository.getById(boardId).orElseThrow();
                entryIds.add(entries.save(new BoardEntryDto(null, board, ns, ew, ns.firstPlayer(), "pass", null, null, 0, 0)));
                entryIds.add(entries.save(new BoardEntryDto(null, board, ew, ns, ew.firstPlayer(), "pass", null, null, 0, 0)));
            }
            var board = boardRepository.getById(boardIds.getLast()).orElseThrow();
            entryIds.add(entries.save(new BoardEntryDto(null, board, null, null, ns.secondPlayer(), "pass", null, null, 0, 0)));
            return new Fixture(owner, director, tournamentId, ns, ew, List.copyOf(boardIds), List.copyOf(entryIds));
        });
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    @BeforeEach
    void resetQueryCount() {
        statistics.clear();
    }

    @Test
    void readsTournamentAndDirectorProfilesWithoutCallerTransaction() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        var tournament = tournamentRepository.getById(fixture.tournamentId());
        assertEquals(Set.of(fixture.owner(), fixture.director()), Set.copyOf(tournament.tds()));
        var profiles = tournaments.getTds(fixture.director()).stream()
                .collect(Collectors.toMap(UserDto::id, profile -> profile));
        assertEquals(Set.of(fixture.owner(), fixture.director()), profiles.keySet());
        assertEquals("Анна", profiles.get(fixture.owner()).name());
        assertEquals("Иванов", profiles.get(fixture.director()).surname());
        assertNull(profiles.get(fixture.director()).username());
    }

    @Test
    void readsMovementCardWithAllBoardsAndPlayerProfilesWithoutCallerTransaction() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        var card = nodes.getAllMovementsForPlayer(fixture.ns().firstPlayer().id(), fixture.tournamentId());
        assertEquals(2, card.size());
        assertEquals(List.of(1, 2), card.getFirst().boards().stream().sorted().toList());
        assertEquals(List.of(3, 4), card.getLast().boards().stream().sorted().toList());
        assertEquals(fixture.ns(), card.getFirst().ns());
        assertEquals(fixture.ew(), card.getFirst().ew());
        assertEquals(fixture.ew(), card.getLast().ns());
        assertEquals(fixture.ns(), card.getLast().ew());
        assertEquals(1, statistics.getPrepareStatementCount(), "The entire card must be loaded in one query");
    }

    @Test
    void readsNextAndSpecificMeetingWithAllBoardsWithoutCallerTransaction() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        UUID player = fixture.ns().firstPlayer().id();
        var next = nodes.findNextNodeForPlayer(player, fixture.tournamentId()).orElseThrow();
        assertEquals(1, next.round());
        assertEquals(List.of(1, 2), next.boards().stream().sorted().toList());
        assertEquals(2, statistics.getPrepareStatementCount(), "Fetch one meeting with SQL pagination, then its boards");
        statistics.clear();
        var byBoard = nodes.findNode(player, fixture.tournamentId(), 1).orElseThrow();
        assertEquals(next.id(), byBoard.id());
        assertEquals(List.of(1, 2), byBoard.boards().stream().sorted().toList());
        assertEquals(fixture.ns(), byBoard.ns());
        assertEquals(fixture.ew(), byBoard.ew());
        assertEquals(2, statistics.getPrepareStatementCount());
    }

    @Test
    void readsPairsAndPlayerProfilesInOneQuery() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        var result = pairs.filterByIds(List.of(fixture.ns().id(), fixture.ew().id()));
        assertEquals(Set.of(fixture.ns(), fixture.ew()), Set.copyOf(result));
        assertEquals(1, statistics.getPrepareStatementCount());
    }

    @Test
    void readsBoardEntriesWithCompleteProfilesWithoutNPlusOne() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        int firstBoard = fixture.boardIds().getFirst();
        var boardEntries = entries.findByBoardId(firstBoard);
        assertEquals(2, boardEntries.size());
        assertEquals(Set.of(fixture.ns(), fixture.ew()), boardEntries.stream().map(BoardEntryDto::ns).collect(Collectors.toSet()));
        assertEquals(Set.of(fixture.ns(), fixture.ew()), boardEntries.stream().map(BoardEntryDto::ew).collect(Collectors.toSet()));
        assertEquals(Set.of(fixture.ns().firstPlayer(), fixture.ew().firstPlayer()),
                boardEntries.stream().map(BoardEntryDto::writer).collect(Collectors.toSet()));
        assertTrue(boardEntries.stream().allMatch(entry -> entry.board().id() == firstBoard && entry.board().number() == 1));
        assertEquals(1, statistics.getPrepareStatementCount());

        statistics.clear();
        var tournamentEntries = entries.findByTournamentId(fixture.tournamentId());
        assertEquals(Set.copyOf(fixture.entryIds()), tournamentEntries.stream().map(BoardEntryDto::id).collect(Collectors.toSet()));
        var standalone = tournamentEntries.stream().filter(entry -> entry.id().equals(fixture.entryIds().getLast())).findFirst().orElseThrow();
        assertNull(standalone.ns());
        assertNull(standalone.ew());
        assertEquals(fixture.ns().secondPlayer(), standalone.writer());
        assertEquals(2, statistics.getPrepareStatementCount(), "Fetch entry IDs, then their full graph in one query");

        statistics.clear();
        assertEquals(fixture.entryIds().getFirst(), entries.getEntryId(fixture.ns().firstPlayer().id(), firstBoard).orElseThrow());
        assertEquals(1, statistics.getPrepareStatementCount());
    }

    private record Fixture(UUID owner, UUID director, UUID tournamentId, PairDto ns, PairDto ew,
                           List<Integer> boardIds, List<UUID> entryIds) {}
}
