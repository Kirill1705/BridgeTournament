package thor.bridge_tournament.core.calculation.tounament;

import thor.bridge_tournament.core.exception.DomainValidationException;
import thor.bridge_tournament.core.type.board.BoardEntry;
import thor.bridge_tournament.core.type.player.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class MedianImpsCalculator implements TournamentCalculator {
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
                if (!entry.isPlayed())
                    continue;
                int diff = entry.getPoints() - avg;
                int imps = translationScale.translate(Math.abs(diff));
                if (diff < 0)
                    imps = -imps;
                result.put(entry, (double) imps);
            }
        }
        return result;
    }

    @Override
    public Map<Pair, Double> rate(List<BoardEntry> entries) {
        Map<BoardEntry, Double> calculatedEntries = calculate(entries);
        Map<Pair, Double> results = new HashMap<>();
        for (Map.Entry<BoardEntry, Double> entry: calculatedEntries.entrySet()) {
            aggregateResult(results, entry.getKey().getNs(), entry.getValue());
            aggregateResult(results, entry.getKey().getEw(), -entry.getValue());
        }
        return results;
    }

    private void aggregateResult(Map<Pair, Double> results, Pair pair, double result) {
        if (!results.containsKey(pair)) {
            results.put(pair, 0d);
        }
        results.put(pair, results.get(pair) + result);
    }

    private Collection<List<BoardEntry>> groupByBoard(List<BoardEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getBoard().uuid()))
                .values();
    }

    private int countAverage(List<BoardEntry> entries) {
        List<BoardEntry> realEntries = entries.stream()
                .filter(BoardEntry::isPlayed)
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
