package thor.bridge_tournament.presentation.telegram.session.state.entry;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.Suit;

import java.util.ArrayList;
import java.util.List;

public class ContractAddEntryState extends AbstractAddEntryState {
    private final List<String> directions = List.of("N", "S", "E", "W");

    @Override
    public List<InlineKeyboardRow> getKeyboardRows() {
        InlineKeyboardRow suits = new InlineKeyboardRow(
                InlineKeyboardButton.builder().callbackData(Suit.NON_TRUMP.name()).text(Suit.NON_TRUMP.getFormattedString()).build(),
                InlineKeyboardButton.builder().callbackData(Suit.SPADES.name()).text(Suit.SPADES.getFormattedString()).build(),
                InlineKeyboardButton.builder().callbackData(Suit.HEARTS.name()).text(Suit.HEARTS.getFormattedString()).build(),
                InlineKeyboardButton.builder().callbackData(Suit.DIAMONDS.name()).text(Suit.DIAMONDS.getFormattedString()).build(),
                InlineKeyboardButton.builder().callbackData(Suit.CLUBS.name()).text(Suit.CLUBS.getFormattedString()).build(),
                InlineKeyboardButton.builder().callbackData("x").text("x").build(),
                InlineKeyboardButton.builder().callbackData("xx").text("xx").build(),
                InlineKeyboardButton.builder().callbackData("pass").text("pass").build()
        );
        InlineKeyboardRow denominations = new InlineKeyboardRow(createNumberButtons());
        InlineKeyboardRow directions = new InlineKeyboardRow(
                InlineKeyboardButton.builder().callbackData("N").text("N").build(),
                InlineKeyboardButton.builder().callbackData("S").text("S").build(),
                InlineKeyboardButton.builder().callbackData("E").text("E").build(),
                InlineKeyboardButton.builder().callbackData("W").text("W").build()
        );
        return List.of(suits, denominations, directions);
    }

    @Override
    protected boolean fillData(String callBackData, SessionWithState<AddEntryData> session) {
        if (callBackData.equals("pass")) {
            session.getData().setModifier(callBackData);
            session.updateState(new ConfirmAddEntryState());
            return false;
        }
        if (callBackData.equalsIgnoreCase("xx") || callBackData.equalsIgnoreCase("x")) {
            session.getData().setModifier(callBackData);
            return nextState(session);
        }
        if (directions.contains(callBackData)) {
            session.getData().setDeclarer(callBackData);
            return nextState(session);
        }
        try {
            Suit suit = Suit.valueOf(callBackData);
            session.getData().setContractSuit(suit);
            return nextState(session);
        } catch (IllegalArgumentException _) {

        }
        session.getData().setDenomination(Integer.parseInt(callBackData));
        return nextState(session);
    }

    @Override
    protected boolean deleteData(AddEntryData data) {
        boolean deleted = data.getContractSuit() != null || data.getDenomination() != null || data.getDeclarer() != null;
        data.setContractSuit(null);
        data.setDeclarer(null);
        data.setDenomination(null);
        return deleted;
    }

    @Override
    protected void revert(SessionWithState<AddEntryData> session) {
        session.updateState(new AddEntryBoardIdState());
    }

    private boolean nextState(SessionWithState<AddEntryData> session) {
        AddEntryData data = session.getData();
        if ("pass".equalsIgnoreCase(data.getModifier()) || data.getContractSuit() != null && data.getDenomination() != null && data.getDeclarer() != null) {
            session.updateState(new LeadAddEntryState());
        }
        return false;
    }

    private List<InlineKeyboardButton> createNumberButtons() {
        List<InlineKeyboardButton> result = new ArrayList<>();
        for (int number = 1; number <= 7; number++) {
            result.add(InlineKeyboardButton.builder()
                    .callbackData(String.valueOf(number))
                    .text(String.valueOf(number))
                    .build()
            );
        }
        return result;
    }
}
