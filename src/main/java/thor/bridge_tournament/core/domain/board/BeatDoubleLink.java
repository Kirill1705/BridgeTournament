package thor.bridge_tournament.core.domain.board;

import thor.bridge_tournament.core.domain.Contract;

import java.util.List;

public class BeatDoubleLink extends BaseBoardCalculatorLink {
    private final List<Integer> nonVulnerable = List.of(100, 300, 500);
    private final List<Integer> vulnerable = List.of(200, 500);

    @Override
    public int calculate(Board board, Direction declarer, Contract contract, int result) {
        if (result < 0 && contract.modifier() != Modifier.PASS) {
            int points;
            if (isVulnerable(declarer, board.getVulnerable())) {
                points = calculateVulnerable(-result, vulnerable);
            }
            else {
                points = calculateVulnerable(-result, nonVulnerable);
            }
            if (contract.modifier() == Modifier.REDOUBLE) {
                points *= 2;
            }
            if (declarer.isNS()) {
                points = -points;
            }
            return points;
        }
        return callNext(board, declarer, contract, result);
    }

    private int calculateVulnerable(int absResult, List<Integer> results) {
        if (absResult <= results.size()) {
            return results.get(absResult - 1);
        }
        return results.getLast() + 300 * (absResult - results.size());
    }
}
