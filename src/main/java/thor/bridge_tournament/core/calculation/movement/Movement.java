package thor.bridge_tournament.core.calculation.movement;

import thor.bridge_tournament.core.type.player.Pair;
import thor.bridge_tournament.core.type.tournament.TournamentNode;
import thor.bridge_tournament.core.type.tournament.TournamentNodeCreator;

import java.util.Collection;
import java.util.List;

public interface Movement {
    Collection<TournamentNode> create(BoardsSelector selector, List<Pair> pairs, TournamentNodeCreator creator);
}
