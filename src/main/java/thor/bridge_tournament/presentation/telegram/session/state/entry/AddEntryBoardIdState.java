package thor.bridge_tournament.presentation.telegram.session.state.entry;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.util.Locale;

@AllArgsConstructor
public class AddEntryBoardIdState implements SessionState<AddEntryData> {

    @Override
    public void sendInfo(TelegramClient client, long chatId, AddEntryData data) throws TelegramApiException {
        String text = "Введите " + data.getBoardLabel().toLowerCase(Locale.ROOT);
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build())
                .build();
        client.execute(message);
    }

    @Override
    public boolean handle(Update update, TelegramClient client, SessionWithState<AddEntryData> session) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            client.execute(AnswerCallbackQuery.builder().callbackQueryId(update.getCallbackQuery().getId()).build());
            return false;
        }
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        try {
            int number = Integer.parseInt(update.getMessage().getText().strip());
            if (number <= 0) {
                throw new NumberFormatException();
            }
            if (!validateBoard(number, client, TelegramUtils.getChatId(update))) {
                return false;
            }
            session.getData().clearEntry();
            session.getData().setBoardNumber(number);
            session.getData().setMessageId(0);
            session.updateState(new ContractAddEntryState(this));
        } catch (NumberFormatException e) {
            SendMessage message = SendMessage.builder()
                    .chatId(TelegramUtils.getChatId(update))
                    .text("Номер или идентификатор сдачи — положительное целое число. Введите снова")
                    .build();
            client.execute(message);
        }
        return false;
    }

    protected boolean validateBoard(int number, TelegramClient client, long chatId) throws TelegramApiException {
        return true;
    }
}
