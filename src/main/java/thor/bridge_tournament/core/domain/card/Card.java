package thor.bridge_tournament.core.domain.card;

public record Card(CardNominal nominal, CardSuit suit) {
    public Card(String name) {
        name = name.toUpperCase();
        char suit = name.charAt(0);
        this(CardNominal.fromName(name.substring(1)), CardSuit.fromStandardName(String.valueOf(suit).toLowerCase()));
    }

    public String toName() {
        return suit.toStandardName() + nominal.toStandardName();
    }
}
