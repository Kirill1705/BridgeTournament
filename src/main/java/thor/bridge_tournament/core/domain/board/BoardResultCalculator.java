package thor.bridge_tournament.core.domain.board;

import thor.bridge_tournament.core.domain.Contract;

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
