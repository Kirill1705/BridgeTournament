package thor.bridge_tournament.core.domain.board;

import lombok.Getter;
import thor.bridge_tournament.core.exception.DomainValidationException;

@Getter
public class Board {
    private final Integer id;
    private final int number;
    private final Direction dealer;
    private final Vulnerable vulnerable;

    public Board(int number) {
        if (number <= 0)
            throw new DomainValidationException("board number should be greater then zero");
        Direction dealer = Direction.values()[(number-1)%4];
        int shift = ((number - 1) / 4) % 4;
        Vulnerable vulnerable = Vulnerable.values()[(number - 1 + shift) % 4];
        this(null, number, dealer, vulnerable);
    }

    public Board(Integer uuid, int number, Direction dealer, Vulnerable vulnerable) {
        if (number <= 0)
            throw new DomainValidationException("board number should be greater then zero");
        this.id = uuid;
        this.number = number;
        this.dealer = dealer;
        this.vulnerable = vulnerable;
    }
}
