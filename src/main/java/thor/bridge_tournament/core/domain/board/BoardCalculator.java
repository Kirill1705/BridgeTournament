package thor.bridge_tournament.core.domain.board;

import thor.bridge_tournament.core.domain.Contract;

public interface BoardCalculator {
    int calculate(Board board, Direction declarer, Contract contract, int result);
}
