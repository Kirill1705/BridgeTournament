package thor.bridge_tournament.core.domain.movement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AverageBoardSelector implements BoardSelector {
    private final List<Integer>[] boards;

    public AverageBoardSelector(List<Integer> boards, int rounds) {
        this.boards = new List[rounds];
        for (int i = 0; i < rounds; i++)
            this.boards[i] = new ArrayList<>();
        for (int i = 0; i < boards.size(); i++) {
            int number = (int)(rounds * i / (double)boards.size());
            this.boards[number].add(boards.get(i));
        }
    }

    @Override
    public List<Integer> select(int round) {
        return Collections.unmodifiableList(boards[round]);
    }

    @Override
    public int rounds() {
        return boards.length;
    }
}
