package thor.bridge_tournament.core.domain;

import thor.bridge_tournament.core.domain.board.Modifier;
import thor.bridge_tournament.core.exception.DomainValidationException;

public record Contract(Denomination denomination, int level, Modifier modifier) {
    public Contract {
        if (level < 0 || level > 7)
            throw new DomainValidationException("Contract level must be between 1 and 7");
        if (modifier == null) {
            throw new DomainValidationException("Contract modifier should be not null");
        }
    }

    public Contract(String name) {
        name = name.toUpperCase();
        Denomination denomination;
        int level = 0;
        Modifier modifier;
        if (name.equals(Modifier.PASS.name())) {
            modifier = Modifier.PASS;
            denomination = null;
        }
        else {
            level = Integer.parseInt(name.substring(0, 1));
            int xxIndex = name.indexOf('X');
            if (xxIndex == -1) {
                xxIndex = name.length();
            }
            denomination = Denomination.valueOf(name.substring(1, xxIndex));
            int xxCount = name.length() - xxIndex;
            if (xxCount == 0) {
                modifier = Modifier.PASS;
            }
            else if (xxCount == 1) {
                modifier = Modifier.DOUBLE;
            }
            else if (xxCount == 2) {
                modifier = Modifier.REDOUBLE;
            }
            else
                throw new IllegalStateException(name);
        }
        this(denomination, level, modifier);
    }

    public String toStandardName() {
        if (denomination == null) {
            return Modifier.PASS.name().toLowerCase();
        }
        return level + denomination.name() + modifier.toStandardName();
    }
}
