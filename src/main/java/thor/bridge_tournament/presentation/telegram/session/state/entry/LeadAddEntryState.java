package thor.bridge_tournament.presentation.telegram.session.state.entry;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.Suit;

import java.util.ArrayList;
import java.util.List;

public class LeadAddEntryState extends AbstractAddEntryState{
    private final List<String> owners = List.of("J", "Q", "K", "A");

    @Override
    public List<InlineKeyboardRow> getKeyboardRows() {
        List<InlineKeyboardButton> owners = List.of(
                InlineKeyboardButton.builder().callbackData("J").text("J").build(),
                InlineKeyboardButton.builder().callbackData("Q").text("Q").build(),
                InlineKeyboardButton.builder().callbackData("K").text("K").build(),
                InlineKeyboardButton.builder().callbackData("A").text("A").build()
        );
        var cardsUpList = createNumberButtons(9, 10);
        cardsUpList.addAll(owners);
        InlineKeyboardRow cardsUp = new InlineKeyboardRow(
                cardsUpList
        );
        InlineKeyboardRow cardsDown = new InlineKeyboardRow(
                createNumberButtons(2, 8)
        );
        InlineKeyboardRow suits = new InlineKeyboardRow(
                InlineKeyboardButton.builder().callbackData(Suit.SPADES.name()).text(Suit.SPADES.getFormattedString()).build(),
                InlineKeyboardButton.builder().callbackData(Suit.HEARTS.name()).text(Suit.HEARTS.getFormattedString()).build(),
                InlineKeyboardButton.builder().callbackData(Suit.DIAMONDS.name()).text(Suit.DIAMONDS.getFormattedString()).build(),
                InlineKeyboardButton.builder().callbackData(Suit.CLUBS.name()).text(Suit.CLUBS.getFormattedString()).build()
        );
        return List.of(cardsDown, cardsUp, suits);
    }

    @Override
    protected boolean fillData(String callBackData, SessionWithState<AddEntryData> session) {
        try {
            Suit suit = Suit.valueOf(callBackData);
            session.getData().setCardSuit(suit);
            return nextState(session);
        } catch (IllegalArgumentException _) {

        }
        session.getData().setNominal(callBackData);
        return nextState(session);
    }

    @Override
    protected boolean deleteData(AddEntryData data) {
        boolean deleted = data.getNominal() != null || data.getCardSuit() != null;
        data.setNominal(null);
        data.setCardSuit(null);
        return deleted;
    }

    @Override
    protected void revert(SessionWithState<AddEntryData> session) {
        session.updateState(new ContractAddEntryState());
    }

    private boolean nextState(SessionWithState<AddEntryData> session) {
        if (session.getData().getCardSuit() != null && session.getData().getNominal() != null) {
            session.updateState(new ResultAddEntryState());
        }
        return false;
    }

    private List<InlineKeyboardButton> createNumberButtons(int from, int to) {
        List<InlineKeyboardButton> result = new ArrayList<>();
        for (int number = from; number <= to; number++) {
            result.add(InlineKeyboardButton.builder()
                    .callbackData(String.valueOf(number))
                    .text(String.valueOf(number))
                    .build()
            );
        }
        return result;
    }
}
