package thor.bridge_tournament.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import thor.bridge_tournament.core.domain.BoardEntryManager;
import thor.bridge_tournament.core.domain.CurrentTournamentManager;
import thor.bridge_tournament.core.domain.tournament.CountType;
import thor.bridge_tournament.core.domain.tournament.Tournament;
import thor.bridge_tournament.core.domain.tournament.TournamentNode;
import thor.bridge_tournament.core.exception.DomainValidationException;
import thor.bridge_tournament.core.exception.TournamentNotFoundException;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.board.BoardDto;
import thor.bridge_tournament.core.port.dto.board.BoardEntryDto;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.output.TransactionalManager;
import thor.bridge_tournament.core.port.output.repository.BoardEntryRepository;
import thor.bridge_tournament.core.port.output.repository.BoardRepository;
import thor.bridge_tournament.core.port.output.repository.TournamentNodeRepository;
import thor.bridge_tournament.core.port.output.repository.UserRepository;
import thor.bridge_tournament.core.service.TournamentBoardEntryServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TournamentDirectorEntryServiceTests {
    private final UserDto td = user();
    private final UUID tournamentId = UUID.randomUUID();
    private final PairDto ns = new PairDto(UUID.randomUUID(), user(), user());
    private final PairDto ew = new PairDto(UUID.randomUUID(), user(), user());
    private final TournamentNode node = new TournamentNode(UUID.randomUUID(), tournamentId, 1, 0, List.of(7), ns, ew);
    private final BoardDto board = new BoardDto(107, 7, "N", "-");
    private final RawBoardEntry raw = new RawBoardEntry("4S", "N", "CK", 0);
    private final Map<UUID, BoardEntryDto> records = new HashMap<>();
    private final Map<UUID, UUID> meetingEntries = new HashMap<>();
    private final CurrentTournamentManager current = mock(CurrentTournamentManager.class);
    private final BoardRepository boards = mock(BoardRepository.class);
    private final BoardEntryRepository entries = mock(BoardEntryRepository.class);
    private final TournamentNodeRepository nodes = mock(TournamentNodeRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TournamentBoardEntryServiceImpl service = new TournamentBoardEntryServiceImpl(current, nodes,
            new TransactionalManager() {
                public <T> T executeTransactional(Supplier<T> action) { return action.get(); }
                public void executeTransactional(Runnable action) { action.run(); }
            }, new BoardEntryManager(current, entries, boards));

    @BeforeEach
    void defaults() {
        when(current.getByTd(td.id())).thenReturn(tournament("IMP", true, Set.of(td.id())));
        when(current.getByPlayerId(ns.firstPlayer().id())).thenReturn(tournament("IMP", true, Set.of(td.id())));
        when(current.getUserRepository()).thenReturn(users);
        when(users.getById(td.id())).thenReturn(td);
        when(users.getById(ns.firstPlayer().id())).thenReturn(ns.firstPlayer());
        when(boards.findByTournament(tournamentId, 7)).thenReturn(Optional.of(board));
        when(boards.getById(board.id())).thenReturn(Optional.of(board));
        for (var player : List.of(ns.firstPlayer(), ns.secondPlayer(), ew.firstPlayer(), ew.secondPlayer())) {
            when(nodes.findNode(player.id(), tournamentId, 7)).thenReturn(Optional.of(node));
        }
        when(entries.findByMeeting(any(), eq(board.id()))).thenAnswer(invocation ->
                Optional.ofNullable(records.get(meetingEntries.get(invocation.getArgument(0)))));
        when(entries.save(any())).thenAnswer(invocation -> {
            BoardEntryDto entry = invocation.getArgument(0);
            UUID id = entry.id() == null ? UUID.randomUUID() : entry.id();
            records.put(id, new BoardEntryDto(id, entry.board(), entry.ns(), entry.ew(), entry.writer(),
                    entry.contract(), entry.declarer(), entry.lead(), entry.result(), entry.points()));
            return id;
        });
        when(entries.findByBoardId(board.id())).thenAnswer(invocation -> List.copyOf(records.values()));
        when(entries.findByTournamentId(tournamentId)).thenAnswer(invocation -> meetingEntries.values().stream()
                .distinct().map(records::get).toList());
        doAnswer(invocation -> {
            meetingEntries.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(nodes).addEntry(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IMP", "MP"})
    void acceptsEitherPairOrderAndWritesFromDirectorsActualId(String countType) {
        when(current.getByTd(td.id())).thenReturn(tournament(countType, true, Set.of(td.id())));
        var result = service.addTournamentBoardEntryByTd(td.id(), 7, raw, ew.secondPlayer().id(), ns.secondPlayer().id());
        var saved = records.get(result.entryId());
        assertEquals(td, saved.writer());
        assertEquals(ns, saved.ns());
        assertEquals(ew, saved.ew());
        assertEquals(countType, result.countType());
        verify(nodes).lock(node.id());
        verify(current, never()).getByPlayerId(any());
    }

    @Test
    void replacesPlayersEntryKeepingItsIdAndMakesDirectorTheAuthor() {
        var playerResult = service.addTournamentBoardEntryForPlayer(ns.firstPlayer().id(), 7, raw);
        var correction = new RawBoardEntry("3NT", "W", "H2", 1);
        var result = service.addTournamentBoardEntryByTd(td.id(), 7, correction, ns.firstPlayer().id(), ew.firstPlayer().id());
        assertEquals(playerResult.entryId(), result.entryId());
        assertEquals(1, records.size());
        assertEquals(1, meetingEntries.size());
        assertEquals(td, records.get(result.entryId()).writer());
        assertEquals("3NT", records.get(result.entryId()).contract());
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryForPlayer(ns.firstPlayer().id(), 7, raw));
        assertEquals("3NT", records.get(result.entryId()).contract());
    }

    @Test
    void sameDirectorCanEnterSameBoardAtTwoDifferentTablesWithoutOverwritingFirst() {
        var otherNs = new PairDto(UUID.randomUUID(), user(), user());
        var otherEw = new PairDto(UUID.randomUUID(), user(), user());
        var otherNode = new TournamentNode(UUID.randomUUID(), tournamentId, 1, 1, List.of(7), otherNs, otherEw);
        when(nodes.findNode(otherNs.firstPlayer().id(), tournamentId, 7)).thenReturn(Optional.of(otherNode));
        var first = service.addTournamentBoardEntryByTd(td.id(), 7, raw, ns.firstPlayer().id(), ew.firstPlayer().id());
        var second = service.addTournamentBoardEntryByTd(td.id(), 7, new RawBoardEntry("pass", null, null, 0),
                otherNs.firstPlayer().id(), otherEw.firstPlayer().id());
        assertNotEquals(first.entryId(), second.entryId());
        assertEquals(2, records.size());
        assertEquals("4S", records.get(first.entryId()).contract());
        assertEquals("pass", records.get(second.entryId()).contract());
        assertEquals(2, second.protocol().size());
    }

    @Test
    void rejectsTwoPlayersFromSamePairAndPlayersOutsideSelectedMeeting() {
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryByTd(td.id(), 7, raw,
                ns.firstPlayer().id(), ns.secondPlayer().id()));
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryByTd(td.id(), 7, raw,
                ns.firstPlayer().id(), UUID.randomUUID()));
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryByTd(td.id(), 7, raw,
                UUID.randomUUID(), ew.firstPlayer().id()));
        assertTrue(records.isEmpty());
        verify(nodes, never()).addEntry(any(), any());
    }

    @Test
    void rejectsNonDirectorEvenIfCurrentTournamentPointerIsStale() {
        when(current.getByTd(td.id())).thenReturn(tournament("IMP", true, Set.of(UUID.randomUUID())));
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryByTd(td.id(), 7, raw,
                ns.firstPlayer().id(), ew.firstPlayer().id()));
        assertTrue(records.isEmpty());
    }

    @Test
    void rejectsMissingDirectorTournamentAndUnstartedTournament() {
        when(current.getByTd(td.id())).thenThrow(new TournamentNotFoundException(td.id()));
        assertThrows(TournamentNotFoundException.class, () -> service.addTournamentBoardEntryByTd(td.id(), 7, raw,
                ns.firstPlayer().id(), ew.firstPlayer().id()));
        doReturn(tournament("IMP", false, Set.of(td.id()))).when(current).getByTd(td.id());
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryByTd(td.id(), 7, raw,
                ns.firstPlayer().id(), ew.firstPlayer().id()));
        assertTrue(records.isEmpty());
    }

    private Tournament tournament(String countType, boolean started, Set<UUID> directors) {
        return new Tournament(td.id(), 8, 4, "Test", tournamentId, directors, CountType.fromStandardName(countType), started);
    }

    private UserDto user() { return new UserDto(UUID.randomUUID(), "user", null, null, 5d); }
}
