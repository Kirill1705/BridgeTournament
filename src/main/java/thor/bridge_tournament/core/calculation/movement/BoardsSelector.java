package thor.bridge_tournament.core.calculation.movement;

import thor.bridge_tournament.core.type.board.Board;

import java.util.List;

public interface BoardsSelector {
    List<Board> select(int round);
    int rounds();
}
