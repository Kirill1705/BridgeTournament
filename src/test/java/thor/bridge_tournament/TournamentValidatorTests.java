package thor.bridge_tournament;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import thor.bridge_tournament.core.calculation.movement.MovementValidator;
import thor.bridge_tournament.core.type.board.Board;
import thor.bridge_tournament.core.type.board.BoardEntry;
import thor.bridge_tournament.core.type.player.Pair;
import thor.bridge_tournament.core.type.tournament.TournamentNode;

import java.util.List;
import java.util.UUID;

public class TournamentValidatorTests {
    @Test
    public void simpleTest_ShouldBeValid() {
        // Arrange
        List<Board> boards = List.of(new Board(1), new Board(2), new Board(3), new Board(4));
        List<Pair> pairs = List.of(newPair(), newPair(), newPair(), newPair());
        List<BoardEntry> entries = List.of(
                new BoardEntry(boards.get(0), pairs.get(0), pairs.get(1)),
                new BoardEntry(boards.get(1), pairs.get(2), pairs.get(3)),
                new BoardEntry(boards.get(2), pairs.get(0), pairs.get(3)),
                new BoardEntry(boards.get(3), pairs.get(1), pairs.get(2))
        );
        List<TournamentNode> tournament = List.of(
                createNode(0, 0, entries.get(0)),
                createNode(0, 1, entries.get(1)),
                createNode(1, 0, entries.get(2)),
                createNode(1, 1, entries.get(3))
        );

        // Assert
        Assertions.assertDoesNotThrow(() -> new MovementValidator().validate(tournament));
    }
    public static Pair newPair() {
        return new Pair(UUID.randomUUID(), null, null);
    }

    private TournamentNode createNode(int round, int table, BoardEntry boardEntry) {
        return new TournamentNode(UUID.randomUUID(), null, round, table, List.of(boardEntry));
    }
}
