package thor.bridge_tournament.presentation.telegram.session.state.entry.data;

public enum Suit {
    NON_TRUMP,
    SPADES,
    HEARTS,
    DIAMONDS,
    CLUBS;
    public String getFormattedString() {
        return switch (this) {
            case NON_TRUMP -> "NT";
            case SPADES -> "♠";
            case HEARTS -> "♥";
            case DIAMONDS -> "♦";
            case CLUBS -> "♣";
        };
    }

    public String toStandardString() {
        return switch (this) {
            case NON_TRUMP -> "NT";
            case SPADES -> "S";
            case HEARTS -> "H";
            case CLUBS -> "C";
            case DIAMONDS -> "D";
        };
    }
}
