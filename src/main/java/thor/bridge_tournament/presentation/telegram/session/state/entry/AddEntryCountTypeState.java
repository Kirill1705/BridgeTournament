package thor.bridge_tournament.presentation.telegram.session.state.entry;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.util.List;

public class AddEntryCountTypeState implements SessionState<AddEntryData> {
    private final List<String> countTypes = List.of("IMP", "MP");

    @Override
    public void sendInfo(TelegramClient client, long chatId, AddEntryData data) throws TelegramApiException {
        ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(new KeyboardRow(countTypes.toArray(new String[0])))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Выберете способ подсчёта")
                .replyMarkup(keyboard)
                .build();
        client.execute(message);
    }

    @Override
    public boolean handle(Update update, TelegramClient client, SessionWithState<AddEntryData> session) throws TelegramApiException {
        String text = update.getMessage().getText().trim().toUpperCase();
        if (!countTypes.contains(text)) {
            SendMessage message = SendMessage.builder()
                    .chatId(TelegramUtils.getChatId(update))
                    .text("Такой способ подсчёта не поддерживается. Используйте кнопки внизу")
                    .build();
            client.execute(message);
            return false;
        }
        session.getData().setCountType(text);
        session.updateState(new AddEntryBoardIdState());
        return false;
    }
}
