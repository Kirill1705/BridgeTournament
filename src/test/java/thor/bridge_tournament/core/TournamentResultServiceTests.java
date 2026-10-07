package thor.bridge_tournament.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import thor.bridge_tournament.core.domain.CurrentTournamentManager;
import thor.bridge_tournament.core.domain.tournament.CountType;
import thor.bridge_tournament.core.domain.tournament.Tournament;
import thor.bridge_tournament.core.exception.TournamentNotFoundException;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.board.BoardDto;
import thor.bridge_tournament.core.port.dto.board.BoardEntryDto;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.output.repository.BoardEntryRepository;
import thor.bridge_tournament.core.service.TournamentResultServiceImpl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TournamentResultServiceTests {
    private final UUID playerId = UUID.randomUUID();
    private final UUID tournamentId = UUID.randomUUID();
    private final CurrentTournamentManager current = mock(CurrentTournamentManager.class);
    private final BoardEntryRepository entries = mock(BoardEntryRepository.class);
    private final TournamentResultServiceImpl service = new TournamentResultServiceImpl(current, entries);

    @ParameterizedTest
    @ValueSource(strings = {"IMP", "MP"})
    void readsPlayersTournamentAndPreservesBothPairProfilesAndEqualRanks(String countType) {
        var ns = pair();
        var ew = pair();
        when(current.getByPlayerId(playerId)).thenReturn(tournament(countType));
        when(entries.findByTournamentId(tournamentId)).thenReturn(List.of(entry(ns, ew, "4S")));

        var result = service.getRanks(playerId);
        assertEquals(2, result.size());
        assertEquals(Set.of(ns, ew), result.stream().map(rank -> rank.pair()).collect(java.util.stream.Collectors.toSet()));
        assertTrue(result.stream().allMatch(rank -> rank.rank() == 1 && rank.countType().equals(countType)));
        assertTrue(result.stream().allMatch(rank -> rank.points() == (countType.equals("MP") ? 50d : 0d)));
        verify(entries).findByTournamentId(tournamentId);
        verify(entries, never()).findByBoardId(anyInt());
        verify(current, never()).getByTd(any());
    }

    @Test
    void directorWithoutPlayerRegistrationCanReadEmptyTournamentResults() {
        when(current.getByPlayerId(playerId)).thenThrow(new TournamentNotFoundException(playerId));
        when(current.getByTd(playerId)).thenReturn(tournament("IMP"));
        when(entries.findByTournamentId(tournamentId)).thenReturn(List.of());
        assertTrue(service.getRanks(playerId).isEmpty());
        verify(current).getByTd(playerId);
    }

    @Test
    void mpOrdersHigherScoresFirstAndUsesCompetitionRanksForTies() {
        var high = pair();
        var equalHigh = pair();
        var low = pair();
        var other1 = pair();
        var other2 = pair();
        var other3 = pair();
        when(current.getByPlayerId(playerId)).thenReturn(tournament("MP"));
        when(entries.findByTournamentId(tournamentId)).thenReturn(List.of(
                entry(low, other1, "1C"), entry(high, other2, "4S"), entry(equalHigh, other3, "4S")));
        var result = service.getRanks(playerId);
        assertEquals(List.of(100d, 75d, 75d, 25d, 25d, 0d),
                result.stream().map(rank -> rank.points()).toList());
        assertEquals(List.of(1, 2, 2, 4, 4, 6), result.stream().map(rank -> rank.rank()).toList());
        assertEquals(other1, result.getFirst().pair());
        assertEquals(low, result.getLast().pair());
    }

    private Tournament tournament(String countType) {
        return new Tournament(UUID.randomUUID(), 8, 4, "Tournament", tournamentId, Set.of(),
                CountType.fromStandardName(countType), true);
    }

    private PairDto pair() {
        return new PairDto(UUID.randomUUID(), profile(), profile());
    }

    private UserDto profile() {
        return new UserDto(UUID.randomUUID(), "player", null, null, 5d);
    }

    private BoardEntryDto entry(PairDto ns, PairDto ew, String contract) {
        return new BoardEntryDto(UUID.randomUUID(), new BoardDto(107, 7, "N", "NO_ONE"), ns, ew,
                ns.firstPlayer(), contract, "N", null, 0, 0);
    }
}
