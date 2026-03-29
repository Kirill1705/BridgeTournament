package thor.bridge_tournament;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import thor.bridge_tournament.core.calculation.tounament.ImpTranslationNode;
import thor.bridge_tournament.core.calculation.tounament.ImpTranslationScale;
import thor.bridge_tournament.core.calculation.tounament.MedianImpsCalculator;
import thor.bridge_tournament.core.calculation.tounament.TournamentCalculator;
import thor.bridge_tournament.core.type.Contract;
import thor.bridge_tournament.core.type.Denomination;
import thor.bridge_tournament.core.type.Direction;
import thor.bridge_tournament.core.type.Modifier;
import thor.bridge_tournament.core.type.board.Board;
import thor.bridge_tournament.core.type.board.BoardEntry;
import thor.bridge_tournament.core.type.card.Card;
import thor.bridge_tournament.core.type.card.CardNominal;
import thor.bridge_tournament.core.type.card.CardSuit;
import thor.bridge_tournament.core.type.player.Pair;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TournamentCalculationTests {
    private ImpTranslationScale scale;
    @Test
    public void simpleTournamentTest() {
        // Arrange
        Board first = new Board(UUID.randomUUID(), 1);
        Board second = new Board(UUID.randomUUID(), 2);
        List<Pair> pairs = List.of(
                new Pair(UUID.randomUUID(), null, null),
                new Pair(UUID.randomUUID(), null, null),
                new Pair(UUID.randomUUID(), null, null),
                new Pair(UUID.randomUUID(), null, null)
        );
        List<BoardEntry> entries = List.of(
                new BoardEntry(UUID.randomUUID(), first, pairs.get(0), pairs.get(1), new Contract(Denomination.H, 4, Modifier.PASS), Direction.N, new Card(CardNominal.ACE, CardSuit.HEARTS), 0),
                new BoardEntry(UUID.randomUUID(), first, pairs.get(2), pairs.get(3), new Contract(Denomination.H, 4, Modifier.DOUBLE), Direction.S, new Card(CardNominal.ACE, CardSuit.HEARTS), -1),
                new BoardEntry(UUID.randomUUID(), second, pairs.get(0), pairs.get(3), new Contract(Denomination.S, 3, Modifier.PASS), Direction.S, new Card(CardNominal.ACE, CardSuit.HEARTS), -1),
                new BoardEntry(UUID.randomUUID(), second, pairs.get(1), pairs.get(2), new Contract(Denomination.D, 2, Modifier.PASS), Direction.E, new Card(CardNominal.ACE, CardSuit.HEARTS), 1)
        );
        TournamentCalculator calculator = new MedianImpsCalculator(0, scale);

        // Act
        Map<BoardEntry, Double> results = calculator.calculate(entries);
        Map<Pair, Double> rates = calculator.rate(entries);

        // Assert
        Assertions.assertEquals(6d, results.get(entries.get(0)));
        Assertions.assertEquals(-6d, results.get(entries.get(1)));
        Assertions.assertEquals(0d, results.get(entries.get(2)));
        Assertions.assertEquals(0d, results.get(entries.get(3)));

        Assertions.assertEquals(6d, rates.get(pairs.get(0)));
        Assertions.assertEquals(-6d, rates.get(pairs.get(1)));
        Assertions.assertEquals(-6d, rates.get(pairs.get(2)));
        Assertions.assertEquals(6d, rates.get(pairs.get(3)));
    }

    @BeforeEach
    public void setUp() {
        scale = new ImpTranslationScale(List.of(
                new ImpTranslationNode(0, 10, 0),
                new ImpTranslationNode(20, 40, 1),
                new ImpTranslationNode(50, 80, 2),
                new ImpTranslationNode(90, 120, 3),
                new ImpTranslationNode(130, 160, 4),
                new ImpTranslationNode(170, 210, 5),
                new ImpTranslationNode(220, 260, 6),
                new ImpTranslationNode(270, 310, 7),
                new ImpTranslationNode(320, 360, 8),
                new ImpTranslationNode(370, 420, 9),
                new ImpTranslationNode(430, 490, 10),
                new ImpTranslationNode(500, 590, 11),
                new ImpTranslationNode(600, 740, 12),
                new ImpTranslationNode(750, 890, 13),
                new ImpTranslationNode(900, 1090, 14),
                new ImpTranslationNode(1100, 1290, 15),
                new ImpTranslationNode(1300, 1490, 16)
        ));
    }
}
