package thor.bridge_tournament.presentation.telegram.session.state.entry;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.BoardResult;

import java.util.ArrayList;
import java.util.List;

public class ResultAddEntryState extends AbstractAddEntryState {
    private final List<String> signs = List.of("=", "-", "+");

    @Override
    public List<InlineKeyboardRow> getKeyboardRows() {
        InlineKeyboardRow extra = new InlineKeyboardRow(
                InlineKeyboardButton.builder().callbackData("=").text("=").build(),
                InlineKeyboardButton.builder().callbackData("+").text("+").build(),
                InlineKeyboardButton.builder().callbackData("-").text("-").build()
        );
        return List.of(extra, new InlineKeyboardRow(createNumberButtons()));
    }

    @Override
    protected boolean fillData(String callBackData, SessionWithState<AddEntryData> session) {
        if (signs.contains(callBackData)) {
            session.getData().setSign(callBackData);
        }
        else {
            session.getData().setResult(Integer.parseInt(callBackData));
        }
        return nextState(session);
    }

    @Override
    protected boolean deleteData(AddEntryData data) {
        boolean deleted = data.getResult() != null;
        data.setResult(null);
        return deleted;
    }

    @Override
    protected void revert(SessionWithState<AddEntryData> session) {
        session.updateState(new LeadAddEntryState());
    }

    private boolean nextState(SessionWithState<AddEntryData> session) {
        if ("=".equals(session.getData().getSign()) || session.getData().getSign() != null && session.getData().getResult() != null) {
            session.updateState(new ConfirmAddEntryState());
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
