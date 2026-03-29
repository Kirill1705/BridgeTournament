package thor.bridge_tournament.core.exception;

import lombok.Getter;

public class TheSameTableInRoundException extends RuntimeException {
    @Getter
    private final int round;
    @Getter
    private final int table;
    public TheSameTableInRoundException(int round, int table) {
        super("The same tables cant be in one round");
        this.round = round;
        this.table = table;
    }
}
