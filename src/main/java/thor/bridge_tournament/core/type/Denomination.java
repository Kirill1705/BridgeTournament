package thor.bridge_tournament.core.type;

public enum Denomination {
    C,
    D,
    H,
    S,
    NT;
    public int getBasePrice(int tricks) {
        return switch (this) {
            case C, D -> 20 * tricks;
            case H, S -> 30 * tricks;
            case NT -> 30 * tricks + 10;
        };
    }

    public int getBasePrice() {
        return switch (this) {
            case C, D -> 20;
            default -> 30;
        };
    }
}
