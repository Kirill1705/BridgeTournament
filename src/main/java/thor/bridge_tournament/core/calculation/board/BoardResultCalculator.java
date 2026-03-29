package thor.bridge_tournament.core.calculation.board;

import thor.bridge_tournament.core.calculation.board.concrete.BeatDoubleLink;
import thor.bridge_tournament.core.calculation.board.concrete.BeatLink;
import thor.bridge_tournament.core.calculation.board.concrete.WinLink;
import thor.bridge_tournament.core.type.Contract;
import thor.bridge_tournament.core.type.Direction;
import thor.bridge_tournament.core.type.board.Board;
import thor.bridge_tournament.core.type.board.BoardEntry;

public enum BoardResultCalculator {
    VALUE;
    private final BoardCalculator calculator;

    BoardResultCalculator() {
        calculator = new WinLink()
                .addNext(new BeatLink())
                .addNext(new BeatDoubleLink());
    }

    public int calculate(Board board, Direction direction, Contract contract, int result) {
        return calculator.calculate(board, direction, contract, result);
    }
}
