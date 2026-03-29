package thor.bridge_tournament.core.type.board;

import thor.bridge_tournament.core.exception.DomainValidationException;
import thor.bridge_tournament.core.type.Direction;
import thor.bridge_tournament.core.type.Vulnerable;

import java.util.UUID;

public class Board {
    private final UUID uuid;
    private final int number;
    private final Direction dealer;
    private final Vulnerable vulnerable;

    public Board(UUID uuid, int number) {
        if (number <= 0)
            throw new DomainValidationException("board number should be greater then zero");
        Direction dealer = Direction.values()[(number-1)%4];
        int shift = ((number - 1) / 4) % 4;
        Vulnerable vulnerable = Vulnerable.values()[(number - 1 + shift) % 4];
        this(uuid, number, dealer, vulnerable);
    }

    public Board(UUID uuid, int number, Direction dealer, Vulnerable vulnerable) {
        if (number <= 0)
            throw new DomainValidationException("board number should be greater then zero");
        this.uuid = uuid;
        this.number = number;
        this.dealer = dealer;
        this.vulnerable = vulnerable;
    }

    public UUID uuid() {
        return uuid;
    }

    public int number() {
        return number;
    }

    public Direction dealer() {
        return dealer;
    }

    public Vulnerable vulnerable() {
        return  vulnerable;
    }

    public Board(int number) {
        this(UUID.randomUUID(), number);
    }
}
