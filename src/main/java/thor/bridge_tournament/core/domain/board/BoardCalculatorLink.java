package thor.bridge_tournament.core.domain.board;

public interface BoardCalculatorLink extends BoardCalculator {
    BoardCalculatorLink addNext(BoardCalculatorLink link);
}
