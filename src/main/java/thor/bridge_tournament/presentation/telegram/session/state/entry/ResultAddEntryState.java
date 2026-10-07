package thor.bridge_tournament.presentation.telegram.session.state.entry;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.BoardResult;

import java.util.ArrayList;
import java.util.List;

public class ResultAddEntryState extends AbstractAddEntryState {
    private final List<String> signs = List.of("=", "-", "+");

    public ResultAddEntryState() {
        this(new LeadAddEntryState());
    }

    public ResultAddEntryState(SessionState<AddEntryData> previousState) {
        super(previousState);
    }

    @Override
    protected String getPrompt() {
        return "Укажите результат кнопками: =, либо + или − и число взяток.";
    }

    @Override
    public List<InlineKeyboardRow> getKeyboardRows() {
        InlineKeyboardRow extra = new InlineKeyboardRow(
                InlineKeyboardButton.builder().callbackData("=").text("=").build(),
                InlineKeyboardButton.builder().callbackData("+").text("+").build(),
                InlineKeyboardButton.builder().callbackData("-").text("-").build()
        );
        var numbers = createNumberButtons();
        return List.of(extra, new InlineKeyboardRow(numbers.subList(0, 7)), new InlineKeyboardRow(numbers.subList(7, numbers.size())));
    }

    @Override
    protected boolean fillData(String callBackData, SessionWithState<AddEntryData> session) {
        if (signs.contains(callBackData)) {
            session.getData().setSign(callBackData);
            if (callBackData.equals("=")) {
                session.getData().setResult(null);
            }
        }
        else {
            session.getData().setResult(Integer.parseInt(callBackData));
        }
        return nextState(session);
    }

    @Override
    protected boolean deleteData(AddEntryData data) {
        boolean deleted = data.getResult() != null || data.getSign() != null;
        data.setResult(null);
        data.setSign(null);
        return deleted;
    }

    private boolean nextState(SessionWithState<AddEntryData> session) {
        if ("=".equals(session.getData().getSign()) || session.getData().getSign() != null && session.getData().getResult() != null) {
            session.updateState(new ConfirmAddEntryState(this));
        }
        return false;
    }

    private List<InlineKeyboardButton> createNumberButtons() {
        List<InlineKeyboardButton> result = new ArrayList<>();
        for (int number = 1; number <= 13; number++) {
            result.add(InlineKeyboardButton.builder()
                    .callbackData(String.valueOf(number))
                    .text(String.valueOf(number))
                    .build()
            );
        }
        return result;
    }
}
