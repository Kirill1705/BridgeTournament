package thor.bridge_tournament.core.exception;

import lombok.Getter;
import thor.bridge_tournament.core.type.player.Pair;

public class PairTableUniqueException extends RuntimeException {
    @Getter
    private final Pair pair;
    @Getter
    private final int round;
    public PairTableUniqueException(Pair pair, int round) {
        super("One pair should be before one table during one round");
        this.pair = pair;
        this.round = round;
    }
}
