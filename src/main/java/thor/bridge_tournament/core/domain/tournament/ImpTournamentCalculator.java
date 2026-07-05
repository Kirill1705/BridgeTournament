package thor.bridge_tournament.core.domain.tournament;

import thor.bridge_tournament.core.domain.board.BoardEntry;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImpTournamentCalculator implements TournamentCalculator {
    @Override
    public List<PairWithResult> calculate(Map<BoardEntry, Double> results) {
        return results.entrySet().stream()
                .flatMap(entry -> Stream.of(
                        Map.entry(entry.getKey().getNs(), entry.getValue()),
                        Map.entry(entry.getKey().getEw(), -entry.getValue())
                ))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingDouble(Map.Entry::getValue)
                )).entrySet().stream()
                .map(entry -> new PairWithResult(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingDouble(PairWithResult::result))
                .toList();
    }
}
