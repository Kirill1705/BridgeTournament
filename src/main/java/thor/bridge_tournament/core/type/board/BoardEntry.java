package thor.bridge_tournament.core.type.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import thor.bridge_tournament.core.calculation.board.BoardResultCalculator;
import thor.bridge_tournament.core.exception.DomainValidationException;
import thor.bridge_tournament.core.type.Contract;
import thor.bridge_tournament.core.type.Direction;
import thor.bridge_tournament.core.type.card.Card;
import thor.bridge_tournament.core.type.player.Pair;

import java.util.UUID;

@Getter
public class BoardEntry {
    private UUID uuid;
    private final Board board;
    private final Pair ns;
    private final Pair ew;

    private Contract contract;
    private Direction declarer;
    private Card lead;
    private int result;

    private int points;

    public BoardEntry(Board board, Pair ns, Pair ew) {
        this.uuid = null;
        this.board = board;
        this.ns = ns;
        this.ew = ew;
    }

    public BoardEntry(UUID uuid, Board board, Pair ns, Pair ew, Contract contract, Direction declarer, Card lead, int result) {
        this(board, ns, ew);
        this.uuid = uuid;
        setResult(contract, declarer, lead, result);
    }

    public void setResult(Contract contract, Direction declarer, Card lead, int result) {
        if (contract == null || declarer == null || lead == null)
            throw new NullPointerException();
        int minResult = -(contract.level() + 6);
        int maxResult = minResult + 13;
        if (result < minResult || result > maxResult)
            throw new DomainValidationException("Result should be between "+minResult+" and "+maxResult);
        points = BoardResultCalculator.VALUE.calculate(board, declarer, contract, result);
        this.contract = contract;
        this.declarer = declarer;
        this.lead = lead;
        this.result = result;
    }

    public boolean isPlayed() {
        return contract != null;
    }
}
