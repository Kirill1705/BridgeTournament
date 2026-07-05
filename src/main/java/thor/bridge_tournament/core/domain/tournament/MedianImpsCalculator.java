package thor.bridge_tournament.core.domain.tournament;

import thor.bridge_tournament.core.domain.board.BoardEntry;
import thor.bridge_tournament.core.exception.DomainValidationException;

import java.util.*;
import java.util.stream.Collectors;

public class MedianImpsCalculator implements BoardCalculator {
    private final double throwAway;
    private final ImpTranslationScale translationScale;

    public MedianImpsCalculator(double throwAway, ImpTranslationScale translationScale) {
        if (throwAway < 0 || throwAway >= 0.5) {
            throw new DomainValidationException("The value for ignoring unusual results should be between 0 and 0.5");
        }
        this.throwAway = throwAway;
        this.translationScale = translationScale;
    }

    public MedianImpsCalculator(ImpTranslationScale translationScale) {
        this(0, translationScale);
    }

    @Override
    public Map<BoardEntry, Double> calculate(List<BoardEntry> entries) {
        Map<BoardEntry, Double> result = new HashMap<>();
        Collection<List<BoardEntry>> grouped = groupByBoard(entries);
        for (List<BoardEntry> entryList: grouped) {
            int avg = countAverage(entryList);
            for (BoardEntry entry: entryList) {
                int diff = entry.getPoints() - avg;
                int imps = translationScale.translate(Math.abs(diff));
                if (diff < 0)
                    imps = -imps;
                result.put(entry, (double) imps);
            }
        }
        return result;
    }

    private Collection<List<BoardEntry>> groupByBoard(List<BoardEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getBoard().getId()))
                .values();
    }

    private int countAverage(List<BoardEntry> entries) {
        List<BoardEntry> realEntries = entries.stream()
                .sorted(Comparator.comparingInt(BoardEntry::getResult))
                .toList();
        int throwCount = (int) (realEntries.size() * throwAway);
        int count = realEntries.size() - throwCount * 2;
        int sum = 0;
        for (int i = throwCount; i < realEntries.size() - throwCount; i++) {
            sum += realEntries.get(i).getPoints();
        }
        return sum/count;
    }
}
