package thor.bridge_tournament.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TournamentBoardEntryServiceTests {
    private final UUID ownerId = UUID.randomUUID();
    private final UUID tournamentId = UUID.randomUUID();
    private final UserDto player = player("initiator");
    private final PairDto ns = new PairDto(UUID.randomUUID(), player("north"), player("south"));
    private final PairDto ew = new PairDto(UUID.randomUUID(), player("east"), player);
    private final BoardDto board = new BoardDto(107, 7, "N", "NO_ONE");
    private final UUID entryId = UUID.randomUUID();
    private final TournamentNode node = new TournamentNode(UUID.randomUUID(), tournamentId, 1, 3, List.of(7, 8), ns, ew);
    private final RawBoardEntry raw = new RawBoardEntry("4S", "N", "CK", 0);
    private final CurrentTournamentManager current = mock(CurrentTournamentManager.class);
    private final TournamentNodeRepository nodes = mock(TournamentNodeRepository.class);
    private final BoardRepository boards = mock(BoardRepository.class);
    private final BoardEntryRepository entries = mock(BoardEntryRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TournamentBoardEntryServiceImpl service = new TournamentBoardEntryServiceImpl(current, nodes,
            new TransactionalManager() {
                @Override
                public <T> T executeTransactional(Supplier<T> action) { return action.get(); }
                @Override
                public void executeTransactional(Runnable action) { action.run(); }
            }, new BoardEntryManager(current, entries, boards));

    @BeforeEach
    void defaults() {
        when(current.getByPlayerId(player.id())).thenReturn(tournament("IMP"));
        when(current.getUserRepository()).thenReturn(users);
        when(users.getById(player.id())).thenReturn(player);
        when(boards.findByTournament(tournamentId, 7)).thenReturn(Optional.of(board));
        when(boards.getById(board.id())).thenReturn(Optional.of(board));
        when(nodes.findNode(player.id(), tournamentId, 7)).thenReturn(Optional.of(node));
        when(entries.findByMeeting(node.id(), board.id())).thenReturn(Optional.empty());
        when(entries.save(any())).thenReturn(entryId);
        when(entries.findByBoardId(board.id())).thenReturn(List.of(savedEntry()));
        when(entries.findByTournamentId(tournamentId)).thenReturn(List.of(savedEntry()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"IMP", "MP"})
    void savesWithInitiatorAsWriterAndMeetingPairsWithoutSubstitutingDirectorOrPartner(String countType) {
        when(current.getByPlayerId(player.id())).thenReturn(tournament(countType));
        var result = service.addTournamentBoardEntryForPlayer(player.id(), 7, raw);

        var saved = ArgumentCaptor.forClass(BoardEntryDto.class);
        verify(entries).save(saved.capture());
        assertEquals(player, saved.getValue().writer());
        assertEquals(ns, saved.getValue().ns());
        assertEquals(ew, saved.getValue().ew());
        assertEquals(board.id(), saved.getValue().board().id());
        verify(nodes).findNode(player.id(), tournamentId, 7);
        verify(current, never()).getByTd(any());
        verify(nodes).addEntry(node.id(), entryId);
        assertEquals(countType, result.countType());
        assertEquals(countType.equals("MP") ? 50d : 0d, result.duplicatePoints());
    }

    @Test
    void directorWhoIsNotAPlayerCannotEnterResultsForSomeoneElse() {
        when(current.getByPlayerId(ownerId)).thenThrow(new TournamentNotFoundException(ownerId));
        when(current.getByTd(ownerId)).thenReturn(tournament("IMP"));

        assertThrows(TournamentNotFoundException.class, () -> service.addTournamentBoardEntryForPlayer(ownerId, 7, raw));
        verify(current, never()).getByTd(any());
        verify(entries, never()).save(any());
        verify(nodes, never()).addEntry(any(), any());
    }

    @Test
    void cannotEnterABoardOutsideInitiatorsPairSchedule() {
        when(nodes.findNode(player.id(), tournamentId, 7)).thenReturn(Optional.empty());
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryForPlayer(player.id(), 7, raw));
        verify(entries, never()).save(any());
        verify(nodes, never()).addEntry(any(), any());
    }

    @Test
    void cannotOverwriteAnotherPlayersResultInTheMeeting() {
        when(entries.findByMeeting(node.id(), board.id())).thenReturn(Optional.of(otherPlayersEntry()));
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryForPlayer(player.id(), 7, raw));
        verify(entries, never()).save(any());
        verify(nodes, never()).addEntry(any(), any());
    }

    @Test
    void standaloneEntryWithSameAuthorAndBoardDoesNotAuthorizeEditingOthersTournamentResult() {
        when(entries.getEntryId(player.id(), board.id())).thenReturn(Optional.of(entryId));
        when(entries.findByMeeting(node.id(), board.id())).thenReturn(Optional.of(otherPlayersEntry()));
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryForPlayer(player.id(), 7, raw));
        verify(entries, never()).save(any());
    }

    @Test
    void authorCanUpdateTheirOwnResultInTheSameMeeting() {
        when(entries.findByMeeting(node.id(), board.id())).thenReturn(Optional.of(savedEntry()));
        assertEquals(entryId, service.addTournamentBoardEntryForPlayer(player.id(), 7, raw).entryId());
        var saved = ArgumentCaptor.forClass(BoardEntryDto.class);
        verify(entries).save(saved.capture());
        assertEquals(entryId, saved.getValue().id());
        assertEquals(player.id(), saved.getValue().writer().id());
    }

    @Test
    void cannotSaveBeforeTournamentStarts() {
        var tournament = tournament("IMP");
        tournament = new Tournament(ownerId, 8, 4, "Tournament", tournamentId, tournament.getTds(),
                tournament.getCountType(), false);
        when(current.getByPlayerId(player.id())).thenReturn(tournament);
        assertThrows(DomainValidationException.class, () -> service.addTournamentBoardEntryForPlayer(player.id(), 7, raw));
        verify(entries, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IMP", "MP"})
    void returnedProtocolExcludesStandaloneOrOtherTournamentEntriesSharingTheBoard(String countType) {
        when(current.getByPlayerId(player.id())).thenReturn(tournament(countType));
        var unrelated = new BoardEntryDto(UUID.randomUUID(), board, ns, ew, ns.firstPlayer(), "7NT", "N", null, 0, 0);
        when(entries.findByBoardId(board.id())).thenReturn(List.of(savedEntry(), unrelated));
        var result = service.addTournamentBoardEntryForPlayer(player.id(), 7, raw);
        assertEquals(1, result.protocol().size());
        assertEquals(entryId, result.protocol().keySet().iterator().next().id());
        assertEquals(countType.equals("MP") ? 50d : 0d, result.duplicatePoints());
    }

    private Tournament tournament(String countType) {
        return new Tournament(ownerId, 8, 4, "Tournament", tournamentId, java.util.Set.of(ownerId),
                CountType.fromStandardName(countType), true);
    }

    private BoardEntryDto savedEntry() {
        return new BoardEntryDto(entryId, board, ns, ew, player, raw.contract(), raw.declarer(), raw.lead(), raw.result(), 420);
    }

    private BoardEntryDto otherPlayersEntry() {
        return new BoardEntryDto(entryId, board, ns, ew, ns.firstPlayer(), raw.contract(), raw.declarer(), raw.lead(), raw.result(), 420);
    }

    private UserDto player(String username) {
        return new UserDto(UUID.randomUUID(), username, null, null, 5d);
    }
}
