package thor.bridge_tournament.core.calculation.board;

import thor.bridge_tournament.core.type.Contract;
import thor.bridge_tournament.core.type.Direction;
import thor.bridge_tournament.core.type.Vulnerable;
import thor.bridge_tournament.core.type.board.Board;
import thor.bridge_tournament.core.type.board.BoardEntry;

public abstract class BaseBoardCalculatorLink implements BoardCalculatorLink {
    private BoardCalculatorLink next;
    @Override
    public BoardCalculatorLink addNext(BoardCalculatorLink link) {
        if (next == null) {
            next = link;
        }
        else {
            next.addNext(link);
        }
        return this;
    }

    protected int callNext(Board board, Direction dealer, Contract contract, int result) {
        if (next == null)
            throw new RuntimeException("Cant calculate board result");
        return next.calculate(board, dealer, contract, result);
    }

    protected boolean isVulnerable(Direction dealer, Vulnerable vulnerable) {
        if (dealer.isNS()) {
            return vulnerable == Vulnerable.NS || vulnerable == Vulnerable.ALL;
        }
        else {
            return vulnerable == Vulnerable.EW || vulnerable == Vulnerable.ALL;
        }
    }
}
