package thor.bridge_tournament.core.domain.tournament;

import thor.bridge_tournament.core.domain.board.BoardEntry;

import java.util.List;
import java.util.Map;

public interface TournamentCalculator {
    List<PairWithResult> calculate(Map<BoardEntry, Double> results);
}
