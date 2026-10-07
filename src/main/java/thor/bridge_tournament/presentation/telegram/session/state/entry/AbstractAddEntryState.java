package thor.bridge_tournament.presentation.telegram.session.state.entry;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
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
    private final SessionState<AddEntryData> previousState;

    protected AbstractAddEntryState(SessionState<AddEntryData> previousState) {
        this.previousState = previousState;
    }

    @Override
    public void sendInfo(TelegramClient client, long chatId, AddEntryData data) throws TelegramApiException {
        sendMessage(client, chatId, data);
    }

    @Override
    public boolean handle(Update update, TelegramClient client, SessionWithState<AddEntryData> session) throws TelegramApiException {
        if (!update.hasCallbackQuery()) {
            return false;
        }
        var callback = update.getCallbackQuery();
        client.execute(AnswerCallbackQuery.builder().callbackQueryId(callback.getId()).build());
        if (callback.getMessage() == null || callback.getMessage().getMessageId() != session.getData().getMessageId()) {
            return false;
        }
        String callBackData = update.getCallbackQuery().getData();
        if ("back".equals(callBackData)) {
            boolean deleted = deleteData(session.getData());
            if (!deleted) {
                revert(session);
            }
            return false;
        }
        if (getKeyboardRows().stream().flatMap(List::stream)
                .noneMatch(button -> button.getCallbackData().equals(callBackData))) {
            return false;
        }
        return fillData(callBackData, session);
    }

    protected abstract List<InlineKeyboardRow> getKeyboardRows();

    protected abstract String getPrompt();

    protected abstract boolean fillData(String callBackData, SessionWithState<AddEntryData> session);

    protected abstract boolean deleteData(AddEntryData data);

    protected void revert(SessionWithState<AddEntryData> session) {
        session.updateState(previousState);
    }

    private void sendMessage(TelegramClient client, long chatId, AddEntryData data) throws TelegramApiException {
        String text = data.toFormattedString() + "\n\n" + getPrompt();
        var builder = InlineKeyboardMarkup.builder();
        List<InlineKeyboardRow> rows = getKeyboardRows();
        for (InlineKeyboardRow row: rows) {
            builder.keyboardRow(row);
        }
        builder.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder().callbackData("back").text("Назад").build()));
        InlineKeyboardMarkup keyboard = builder.build();
        if (data.getMessageId() == 0) {
            data.setMessageId(client.execute(SendMessage.builder().chatId(chatId).text(text)
                    .replyMarkup(keyboard).build()).getMessageId());
        } else {
            client.execute(EditMessageText.builder().chatId(chatId).messageId(data.getMessageId())
                    .text(text).replyMarkup(keyboard).build());
        }
    }
}
