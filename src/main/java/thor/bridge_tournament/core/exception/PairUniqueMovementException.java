package thor.bridge_tournament.core.exception;

import lombok.Getter;

public class PairUniqueMovementException extends RuntimeException {
    @Getter
    private final int first;
    @Getter
    private final int second;
    public PairUniqueMovementException(int first, int second) {
        super("This two pairs can play only once");
        this.first = first;
        this.second = second;
    }
}
