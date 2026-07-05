package thor.bridge_tournament.core.domain.card;

public enum CardSuit {
    CLUBS,
    DIAMONDS,
    HEARTS,
    SPADES;
    public String toStandardName() {
        return switch (this) {
            case CLUBS -> "c";
            case DIAMONDS -> "d";
            case HEARTS -> "h";
            case SPADES -> "s";
        };
    }
    public static CardSuit fromStandardName(String name) {
        return switch (name) {
            case "s" -> SPADES;
            case "c" -> CLUBS;
            case "d" -> DIAMONDS;
            case "h" -> HEARTS;
            default -> throw new IllegalStateException(name);
        };
    }
}
