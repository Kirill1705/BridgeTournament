package thor.bridge_tournament.core.domain.card;

public enum CardNominal {
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    JACK,
    QUEEN,
    KING,
    ACE;
    public static CardNominal fromName(String name) {
        if (Character.isDigit(name.charAt(0))) {
            return CardNominal.values()[Integer.parseInt(name) - 2];
        }
        return switch (name) {
            case "J" -> CardNominal.JACK;
            case "Q" -> CardNominal.QUEEN;
            case "K" -> CardNominal.KING;
            case "A" -> CardNominal.ACE;
            default -> throw new IllegalStateException("Unexpected value: " + name);
        };
    }

    public String toStandardName() {
        if (this.ordinal() <= 8) {
            return String.valueOf(this.ordinal() + 2);
        }
        return this.name().substring(0, 1);
    }
}
