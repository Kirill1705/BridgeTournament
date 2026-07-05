package thor.bridge_tournament.core.exception;

import lombok.Getter;

public class DealsUniqueMovementException extends RuntimeException {
    @Getter
    private final int board;
    @Getter
    private final int pair;

    public DealsUniqueMovementException(int board, int pair) {
        super("Pair cant play one board twice");
        this.board = board;
        this.pair = pair;
    }
}
