package thor.bridge_tournament.core.calculation.board;

public interface BoardCalculatorLink extends BoardCalculator {
    BoardCalculatorLink addNext(BoardCalculatorLink link);
}
