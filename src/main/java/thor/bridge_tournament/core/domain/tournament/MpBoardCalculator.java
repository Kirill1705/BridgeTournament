package thor.bridge_tournament.core.domain.tournament;

import thor.bridge_tournament.core.domain.board.BoardEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MpBoardCalculator implements BoardCalculator {
    @Override
    public Map<BoardEntry, Double> calculate(List<BoardEntry> entries) {
        Map<BoardEntry, Double> result = new HashMap<>();
        var boards = entries.stream().collect(Collectors.groupingBy(entry -> entry.getBoard().getId()));
        for (var boardEntries : boards.values()) {
            for (var entry : boardEntries) {
                long lower = boardEntries.stream().filter(other -> other.getPoints() < entry.getPoints()).count();
                long equal = boardEntries.stream().filter(other -> other.getPoints() == entry.getPoints()).count() - 1;
                double percent = boardEntries.size() == 1 ? 50d
                        : 100d * (2 * lower + equal) / (2 * (boardEntries.size() - 1));
                result.put(entry, percent);
            }
        }
        return result;
    }
}
