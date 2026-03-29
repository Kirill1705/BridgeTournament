package thor.bridge_tournament.core.type;

public enum Direction {
    N,
    E,
    S,
    W;
    public boolean isNS() {
        return this == N || this == S;
    }
    public boolean isEW() {
        return !isNS();
    }
}
