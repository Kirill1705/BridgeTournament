package thor.bridge_tournament.core;

import org.junit.jupiter.api.Test;
import thor.bridge_tournament.core.domain.Contract;
import thor.bridge_tournament.core.domain.board.*;
import thor.bridge_tournament.core.domain.tournament.*;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TournamentCalculationTests {
    private final PairDto ns = new PairDto(UUID.randomUUID(), null, null);
    private final PairDto ew = new PairDto(UUID.randomUUID(), null, null);

    @Test
    void generatesPositiveBoardNumbersAndSkipsExistingBoards() {
        Tournament tournament = new Tournament(UUID.randomUUID(), 4, 2, CountType.MEDIAN_IMPS, "Test");
        assertEquals(List.of(1, 2, 3, 4), tournament.generateBoards(Set.of()).stream().map(Board::getNumber).toList());
        assertEquals(List.of(2, 4), tournament.generateBoards(Set.of(1, 3)).stream().map(Board::getNumber).toList());
        assertTrue(tournament.generateBoards(Set.of(1, 2, 3, 4)).isEmpty());
    }

    @Test
    void ranksHighestImpTotalFirst() {
        var result = new ImpTournamentCalculator().calculate(Map.of(entry("4S", 0), 10d));
        assertEquals(List.of(10d, -10d), result.stream().map(PairWithResult::result).toList());
        assertEquals(ns, result.getFirst().pair());
    }

    @Test
    void ranksHighestMpAverageFirst() {
        var result = new MpTournamentCalculator().calculate(Map.of(entry("4S", 0), 70d));
        assertEquals(List.of(70d, 30d), result.stream().map(PairWithResult::result).toList());
        assertEquals(ns, result.getFirst().pair());
    }

    @Test
    void trimsExtremePointsInsteadOfExtremeOvertricks() {
        BoardEntry partScore = entry("1C", 0);
        BoardEntry slam = entry("7NT", 0);
        BoardEntry game = entry("4S", 1);
        var entries = List.of(partScore, slam, game, entry("4S", 1), entry("4S", 1));
        var calculator = new MedianImpsCalculator(.2, ImpTranslationScale.createDefault());
        var results = calculator.calculate(entries);
        assertEquals(0d, results.get(game));
        assertEquals(-9d, results.get(partScore));
        assertEquals(14d, results.get(slam));
        assertEquals(results, calculator.calculate(entries.reversed()));
    }

    private BoardEntry entry(String contract, int result) {
        return new BoardEntry(UUID.randomUUID(), new Board(1, 1, Direction.N, Vulnerable.NO_ONE),
                ns, ew, new Contract(contract), Direction.N, null, result, null);
    }
}
