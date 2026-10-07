package thor.bridge_tournament.presentation.telegram.session.state.entry;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.util.List;

public class ConfirmAddEntryState extends AbstractAddEntryState{
    public ConfirmAddEntryState() {
        this(new ResultAddEntryState());
    }

    public ConfirmAddEntryState(SessionState<AddEntryData> previousState) {
        super(previousState);
    }

    @Override
    protected String getPrompt() {
        return "Проверьте введённые данные и нажмите «Подтвердить», чтобы сохранить результат.";
    }

    @Override
    protected List<InlineKeyboardRow> getKeyboardRows() {
        InlineKeyboardRow ok = new InlineKeyboardRow(
                InlineKeyboardButton.builder().callbackData("ok").text("Подтвердить").build()
        );
        return List.of(ok);
    }

    @Override
    protected boolean fillData(String callBackData, SessionWithState<AddEntryData> session) {
        return "ok".equals(callBackData);
    }

    @Override
    protected boolean deleteData(AddEntryData data) {
        if ("pass".equals(data.getModifier())) {
            data.setModifier(null);
        }
        return false;
    }
}
