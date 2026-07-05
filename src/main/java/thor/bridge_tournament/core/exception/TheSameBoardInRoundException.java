package thor.bridge_tournament.core.exception;

import lombok.Getter;

public class TheSameBoardInRoundException extends RuntimeException {
    @Getter
    private final int board;
    public TheSameBoardInRoundException(int board) {
        super("The same boards could not be played in one round");
        this.board = board;
    }
}
