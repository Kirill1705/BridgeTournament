package thor.bridge_tournament.presentation.telegram.session.state.entry.data;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public class AddEntryData {
    @Setter
    private int messageId;

    @Setter
    private Suit contractSuit;
    @Setter
    private Integer denomination;
    @Setter
    private String modifier;
    @Setter
    private String declarer;

    @Setter
    private Suit cardSuit;
    @Setter
    private String nominal;

    @Setter
    private String sign;
    @Setter
    private Integer result;

    @Setter
    private int boardNumber;

    @Setter
    private String countType;

    public String toFormattedString() {
        return "Контракт: " +
                toStringOrEmpty(denomination, String::valueOf) +
                toStringOrEmpty(contractSuit, Suit::getFormattedString) +
                toStringOrEmpty(modifier, modifier -> modifier) +
                " " +
                toStringOrEmpty(declarer, declarer1 -> declarer1) +
                "\n" +
                "Атака: " +
                toStringOrEmpty(cardSuit, Suit::getFormattedString) +
                toStringOrEmpty(nominal, nominal -> nominal) +
                "\n" +
                "Результат: " +
                toStringOrEmpty(sign, sign -> sign) +
                toStringOrEmpty(result, result -> String.valueOf(Math.abs(result)));
    }

    public int getResultInt() {
        if (sign.equals("=")) {
            return 0;
        }
        else if (sign.equals("-")) {
            return -result;
        }
        return result;
    }

    public String getContractString() {
        return toStringOrEmpty(denomination, String::valueOf) +
                toStringOrEmpty(contractSuit, Suit::toStandardString) +
                toStringOrEmpty(modifier, modifier -> modifier);
    }

    public String getLeadString() {
        if (cardSuit == null) {
            return null;
        }
        return toStringOrEmpty(cardSuit, Suit::toStandardString) +
                toStringOrEmpty(nominal, nominal -> nominal);
    }

    private <T> String toStringOrEmpty(T object, Function<T, String> mapFunction) {
        return object != null ? mapFunction.apply(object) : "";
    }
}
