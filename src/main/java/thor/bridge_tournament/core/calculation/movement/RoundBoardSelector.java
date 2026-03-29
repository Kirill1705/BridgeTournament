package thor.bridge_tournament.core.calculation.movement;

import thor.bridge_tournament.core.type.board.Board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoundBoardSelector implements BoardsSelector {
    private final List<Board>[] boards;

    public RoundBoardSelector(List<Board> boards, int rounds) {
        this.boards = new List[rounds];
        for (int i = 0; i < rounds; i++)
            this.boards[i] = new ArrayList<>();
        for (int i = 0; i < boards.size(); i++) {
            int number = (int)(rounds * i / (double)boards.size());
            this.boards[number].add(boards.get(i));
        }
    }

    @Override
    public List<Board> select(int round) {
        return Collections.unmodifiableList(boards[round]);
    }

    @Override
    public int rounds() {
        return boards.length;
    }
}
