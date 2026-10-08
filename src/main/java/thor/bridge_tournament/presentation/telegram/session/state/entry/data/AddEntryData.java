package thor.bridge_tournament.presentation.telegram.session.state.entry.data;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.presentation.telegram.TournamentMessages;

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

    @Setter
    private String boardLabel = "Идентификатор сдачи";

    @Setter
    private UserDto firstPairPlayer;
    @Setter
    private UserDto secondPairPlayer;

    public String toFormattedString() {
        String meeting = firstPairPlayer == null || secondPairPlayer == null ? ""
                : "Судейский ввод. Игроки выбранных пар: " + TournamentMessages.user(firstPairPlayer)
                + " — " + TournamentMessages.user(secondPairPlayer) + "\n";
        return meeting + boardLabel + ": " + boardNumber + "\nКонтракт: " +
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

    public void clearEntry() {
        contractSuit = null;
        denomination = null;
        modifier = null;
        declarer = null;
        cardSuit = null;
        nominal = null;
        sign = null;
        result = null;
    }

    private <T> String toStringOrEmpty(T object, Function<T, String> mapFunction) {
        return object != null ? mapFunction.apply(object) : "";
    }
}
