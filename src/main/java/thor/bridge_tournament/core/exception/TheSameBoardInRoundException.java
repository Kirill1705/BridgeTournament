package thor.bridge_tournament.core.exception;

import lombok.Getter;
import thor.bridge_tournament.core.type.board.Board;

public class TheSameBoardInRoundException extends RuntimeException {
    @Getter
    private final Board board;
    public TheSameBoardInRoundException(Board board) {
        super("The same boards could not be played in one round");
        this.board = board;
    }
}
