package thor.bridge_tournament.core.domain.board;

public enum Vulnerable {
    NO_ONE,
    NS,
    EW,
    ALL;
    public static Vulnerable fromName(String name) {
        if (name.equals("-")) {
            return Vulnerable.NO_ONE;
        }
        return Vulnerable.valueOf(name.toUpperCase());
    }

    @Override
    public String toString() {
        if (this == NO_ONE) {
            return "-";
        }
        return super.toString();
    }
}
