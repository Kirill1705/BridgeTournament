package thor.bridge_tournament.core.domain.board;

public enum Modifier {
    DOUBLE,
    REDOUBLE,
    PASS;
    public int getMultiplier() {
        return switch (this) {
            case PASS -> 1;
            case DOUBLE -> 2;
            case REDOUBLE -> 4;
        };
    }

    public int getPointsIfWin() {
        return switch (this) {
            case PASS -> 0;
            case DOUBLE -> 50;
            case REDOUBLE -> 100;
        };
    }

    public int getAdditionalPointsPerTrick() {
        return switch (this) {
            case PASS -> 0;
            case DOUBLE -> 100;
            case REDOUBLE -> 200;
        };
    }

    public String toStandardName() {
        return switch (this) {
            case PASS -> "";
            case DOUBLE -> "x";
            case REDOUBLE -> "xx";
        };
    }
}
