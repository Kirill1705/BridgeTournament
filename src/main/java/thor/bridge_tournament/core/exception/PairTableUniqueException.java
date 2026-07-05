package thor.bridge_tournament.core.exception;

import lombok.Getter;

public class PairTableUniqueException extends RuntimeException {
    @Getter
    private final int pair;
    @Getter
    private final int round;
    public PairTableUniqueException(int pair, int round) {
        super("One pair should be on one table during one round");
        this.pair = pair;
        this.round = round;
    }
}
