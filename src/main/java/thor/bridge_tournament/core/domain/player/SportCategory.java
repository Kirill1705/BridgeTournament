package thor.bridge_tournament.core.domain.player;

import thor.bridge_tournament.core.exception.DomainValidationException;

public record SportCategory(Double value) {
    private static final double defaultSportCategory = 5.0;

    public SportCategory {
        if (value == null) {
            value = defaultSportCategory;
        }
        if (value < -5 || value > 5)
            throw new DomainValidationException("sport category must be between -5 and 5");
    }
}
