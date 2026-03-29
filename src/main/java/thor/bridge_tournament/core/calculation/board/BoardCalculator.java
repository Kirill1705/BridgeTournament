package thor.bridge_tournament.core.calculation.board;

import thor.bridge_tournament.core.type.Contract;
import thor.bridge_tournament.core.type.Direction;
import thor.bridge_tournament.core.type.board.Board;
import thor.bridge_tournament.core.type.board.BoardEntry;

public interface BoardCalculator {
    int calculate(Board board, Direction declarer, Contract contract, int result);
}
