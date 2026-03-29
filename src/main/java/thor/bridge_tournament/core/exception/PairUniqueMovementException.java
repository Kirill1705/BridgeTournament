package thor.bridge_tournament.core.exception;

import lombok.Getter;
import thor.bridge_tournament.core.type.player.Pair;

public class PairUniqueMovementException extends RuntimeException {
    @Getter
    private final Pair first;
    @Getter
    private final Pair second;
    public PairUniqueMovementException(Pair first, Pair second) {
        this.first = first;
        this.second = second;
        super("This two pairs can play only once");
    }
}
