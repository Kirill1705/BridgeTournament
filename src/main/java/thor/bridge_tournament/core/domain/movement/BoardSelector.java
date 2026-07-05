package thor.bridge_tournament.core.domain.movement;

import java.util.List;

public interface BoardSelector {
    List<Integer> select(int round);
    int rounds();
}
