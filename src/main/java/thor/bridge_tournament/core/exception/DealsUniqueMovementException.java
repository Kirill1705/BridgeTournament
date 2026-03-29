package thor.bridge_tournament.core.exception;

import lombok.Getter;
import thor.bridge_tournament.core.type.board.Board;
import thor.bridge_tournament.core.type.player.Pair;

public class DealsUniqueMovementException extends RuntimeException {
    @Getter
    private final Board board;
    @Getter
    private final Pair pair;

    public DealsUniqueMovementException(Board board, Pair pair) {
        this.board = board;
        this.pair = pair;
        super("Pair cant play one board twice");
    }
}
