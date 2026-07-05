package thor.bridge_tournament.core.exception;

import lombok.Getter;

@Getter
public class MovementNotFoundException extends RuntimeException {
    private final int pairsCount;

    public MovementNotFoundException(int pairsCount) {
        super("Movement with given number of pairs not found");
        this.pairsCount = pairsCount;
    }
}
