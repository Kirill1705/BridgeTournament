package thor.bridge_tournament.core.domain.board;

import thor.bridge_tournament.core.domain.Contract;

public class BeatLink extends BaseBoardCalculatorLink {
    @Override
    public int calculate(Board board, Direction declarer, Contract contract, int result) {
        if (result < 0 && contract.modifier() == Modifier.PASS) {
            int points;
            if (isVulnerable(declarer, board.getVulnerable())) {
                points = -result * 100;
            }
            else {
                points = -result * 50;
            }
            if (declarer.isNS()) {
                points = -points;
            }
            return points;
        }
        return callNext(board, declarer, contract, result);
    }
}
