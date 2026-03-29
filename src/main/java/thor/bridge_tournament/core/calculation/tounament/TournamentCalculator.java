package thor.bridge_tournament.core.calculation.tounament;

import thor.bridge_tournament.core.type.board.BoardEntry;
import thor.bridge_tournament.core.type.player.Pair;

import java.util.List;
import java.util.Map;

public interface TournamentCalculator {
    Map<BoardEntry, Double> calculate(List<BoardEntry> entries);
    Map<Pair, Double> rate(List<BoardEntry> entries);
}
