package thor.bridge_tournament.core.type;

import thor.bridge_tournament.core.exception.DomainValidationException;

public record Contract(Denomination denomination, int level, Modifier modifier) {
    public Contract {
        if (level < 0 || level > 7)
            throw new DomainValidationException("Contract level must be between 1 and 7");
    }
}
