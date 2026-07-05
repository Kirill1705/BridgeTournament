package thor.bridge_tournament.core.exception;

import lombok.Getter;

@Getter
public class BoardNotFoundException extends RuntimeException {
    private final int boardNumber;

    public BoardNotFoundException(int boardNumber) {
        super("Board with number " + boardNumber + " not found in this tournament");
        this.boardNumber = boardNumber;
    }
}
