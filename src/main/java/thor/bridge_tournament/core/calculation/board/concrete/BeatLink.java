package thor.bridge_tournament.core.calculation.board.concrete;

import thor.bridge_tournament.core.calculation.board.BaseBoardCalculatorLink;
import thor.bridge_tournament.core.type.Contract;
import thor.bridge_tournament.core.type.Direction;
import thor.bridge_tournament.core.type.Modifier;
import thor.bridge_tournament.core.type.board.Board;

public class BeatLink extends BaseBoardCalculatorLink {
    @Override
    public int calculate(Board board, Direction declarer, Contract contract, int result) {
        if (result < 0 && contract.modifier() == Modifier.PASS) {
            int points;
            if (isVulnerable(declarer, board.vulnerable())) {
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
