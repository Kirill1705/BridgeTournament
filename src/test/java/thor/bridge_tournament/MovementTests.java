package thor.bridge_tournament;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import thor.bridge_tournament.core.calculation.movement.BoardsSelector;
import thor.bridge_tournament.core.calculation.movement.howell.HowellMovement;
import thor.bridge_tournament.core.calculation.movement.MovementValidator;
import thor.bridge_tournament.core.calculation.movement.RoundBoardSelector;
import thor.bridge_tournament.core.type.board.Board;
import thor.bridge_tournament.core.type.player.Pair;
import thor.bridge_tournament.core.type.tournament.TournamentNode;
import thor.bridge_tournament.core.type.tournament.TournamentNodeCreatorImpl;

import java.util.*;

public class MovementTests {
    @Test
    public void howellMovementTest() {
        // Arrange
        List<Board> boards = List.of(new Board(1), new Board(2), new Board(3), new Board(4), new Board(5));
        List<Pair> pairs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            pairs.add(TournamentValidatorTests.newPair());
        }
        BoardsSelector selector = new RoundBoardSelector(boards, boards.size());

        // Act
        Collection<TournamentNode> nodes = new HowellMovement().create(selector, pairs, new TournamentNodeCreatorImpl(UUID.randomUUID()));

        // Assert
        Assertions.assertDoesNotThrow(() -> new MovementValidator().validate(nodes));
    }

    @Test
    public void speedTest_ShouldCreateBigTournament() {
        // Arrange
        int n = 4;
        List<Board> boards = new ArrayList<>();
        List<Pair> pairs = new ArrayList<>();
        for (int i = 0; i < 2*n - 1; i++) {
            boards.add(new Board(i+1));
            pairs.add(new Pair(UUID.randomUUID(), null, null));
        }
        pairs.add(new Pair(UUID.randomUUID(), null, null));
        BoardsSelector selector = new RoundBoardSelector(boards, boards.size());

        // Act
        Collection<TournamentNode> nodes = new HowellMovement().create(selector, pairs, new TournamentNodeCreatorImpl(UUID.randomUUID()));

        // Assert
        Assertions.assertDoesNotThrow(() -> new MovementValidator().validate(nodes));
    }
}
