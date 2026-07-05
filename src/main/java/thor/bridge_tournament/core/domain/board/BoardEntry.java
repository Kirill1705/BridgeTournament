package thor.bridge_tournament.core.domain.board;

import lombok.Getter;
import thor.bridge_tournament.core.domain.Contract;
import thor.bridge_tournament.core.domain.card.Card;
import thor.bridge_tournament.core.domain.player.User;
import thor.bridge_tournament.core.exception.DomainValidationException;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;

import java.util.UUID;

@Getter
public class BoardEntry {
    private final UUID uuid;
    private final Board board;
    private final PairDto ns;
    private final PairDto ew;
    private final User writer;

    private Contract contract;
    private Direction declarer;
    private Card lead;
    private int result;

    private int points;

    public BoardEntry(UUID uuid, Board board, PairDto ns, PairDto ew, Contract contract, Direction declarer, Card lead, int result, User writer) {
        this.board = board;
        this.ns = ns;
        this.ew = ew;
        this.uuid = uuid;
        this.writer = writer;
        setResult(contract, declarer, lead, result);
    }

    public void setResult(Contract contract, Direction declarer, Card lead, int result) {
        if (contract == null)
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
}
