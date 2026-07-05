package thor.bridge_tournament.presentation.telegram.session.state.entry;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.util.List;

public abstract class AbstractAddEntryState implements SessionState<AddEntryData> {

    @Override
    public void sendInfo(TelegramClient client, long chatId, AddEntryData data) throws TelegramApiException {
        sendMessage(client, chatId, data);
    }

    @Override
    public boolean handle(Update update, TelegramClient client, SessionWithState<AddEntryData> session) throws TelegramApiException {
        String callBackData = update.getCallbackQuery().getData();
        if (callBackData.equals("back")) {
            boolean deleted = deleteData(session.getData());
            if (!deleted) {
                revert(session);
            }
            return false;
        }
        return fillData(callBackData, session);
    }

    protected abstract List<InlineKeyboardRow> getKeyboardRows();

    protected abstract boolean fillData(String callBackData, SessionWithState<AddEntryData> session);

    protected abstract boolean deleteData(AddEntryData data);

    protected abstract void revert(SessionWithState<AddEntryData> session);

    private void sendMessage(TelegramClient client, long chatId, AddEntryData data) throws TelegramApiException {
        var messageBuilder = EditMessageText.builder()
                .chatId(chatId)
                .messageId(data.getMessageId())
                .text(data.toFormattedString());
        var builder = InlineKeyboardMarkup.builder();
        List<InlineKeyboardRow> rows = getKeyboardRows();
        for (InlineKeyboardRow row: rows) {
            builder.keyboardRow(row);
        }
        builder.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().callbackData("back").text("Назад").build()));
        InlineKeyboardMarkup keyboard = builder.build();
        messageBuilder.replyMarkup(keyboard);
        client.execute(messageBuilder.build());
    }
}
