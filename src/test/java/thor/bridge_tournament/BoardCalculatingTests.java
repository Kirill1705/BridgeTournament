package thor.bridge_tournament;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import thor.bridge_tournament.core.calculation.board.BoardResultCalculator;
import thor.bridge_tournament.core.type.*;
import thor.bridge_tournament.core.type.board.Board;

public class BoardCalculatingTests {
    @Test
    public void simpleContractTest() {
        // Arrange
        Board board = new Board(null, 1, Direction.N, Vulnerable.NO_ONE);

        // Act
        int points = BoardResultCalculator.VALUE.calculate(board, Direction.N, new Contract(
                Denomination.H,
                2,
                Modifier.PASS
        ), 1);

        // Assert
        Assertions.assertEquals(140, points);
    }

    @Test
    public void winContractTest() {
        // Arrange
        Board board = new Board(null, 2);

        // Act
        int points = BoardResultCalculator.VALUE.calculate(board, Direction.E, new Contract(
                Denomination.C,
                5,
                Modifier.PASS
        ), 0);

        // Assert
        Assertions.assertEquals(-400, points);
    }

    @Test
    public void winContractDoubledTest() {
        // Arrange
        Board board = new Board(null, 3);

        // Act
        int points = BoardResultCalculator.VALUE.calculate(board, Direction.W, new Contract(
                Denomination.D,
                4,
                Modifier.DOUBLE
        ), 1);

        // Assert
        Assertions.assertEquals(-910, points);
    }

    @Test
    public void winContractRedoubleTest() {
        // Arrange
        Board board = new Board(null, 4);

        // Act
        int points = BoardResultCalculator.VALUE.calculate(board, Direction.N, new Contract(
                Denomination.NT,
                6,
                Modifier.REDOUBLE
        ), 1);

        // Assert
        Assertions.assertEquals(2510, points);
    }

    @Test
    public void defeatContractTest() {
        // Arrange
        Board board = new Board(null, 9);

        // Act
        int points = BoardResultCalculator.VALUE.calculate(board, Direction.E, new Contract(
                Denomination.S,
                5,
                Modifier.PASS
        ), -3);

        // Assert
        Assertions.assertEquals(300, points);
    }

    @Test
    public void defeatContractDoubledTest() {
        // Arrange
        Board board = new Board(null, 33);

        // Act
        int points = BoardResultCalculator.VALUE.calculate(board, Direction.S, new Contract(
                Denomination.C,
                5,
                Modifier.DOUBLE
        ), -4);

        // Assert
        Assertions.assertEquals(-800, points);
    }
}
