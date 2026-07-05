package thor.bridge_tournament.presentation.telegram.session.state.entry;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.util.List;

public class ConfirmAddEntryState extends AbstractAddEntryState{
    @Override
    protected List<InlineKeyboardRow> getKeyboardRows() {
        InlineKeyboardRow ok = new InlineKeyboardRow(
                InlineKeyboardButton.builder().callbackData("ok").text("Подтвердить").build()
        );
        return List.of(ok);
    }

    @Override
    protected boolean fillData(String callBackData, SessionWithState<AddEntryData> session) {
        return true;
    }

    @Override
    protected boolean deleteData(AddEntryData data) {
        return false;
    }

    @Override
    protected void revert(SessionWithState<AddEntryData> session) {
        session.updateState(new ResultAddEntryState());
    }
}
