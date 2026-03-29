package thor.bridge_tournament.core.type.player;

import thor.bridge_tournament.core.exception.DomainValidationException;

public record SportCategory(double value) {
    public SportCategory {
        if (value < -5 || value > 5)
            throw new DomainValidationException("sport category must be between -5 and 5");
    }
}
