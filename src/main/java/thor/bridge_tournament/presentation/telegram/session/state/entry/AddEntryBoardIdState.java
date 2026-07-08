package thor.bridge_tournament.presentation.telegram.session.state.entry;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

@AllArgsConstructor
public class AddEntryBoardIdState implements SessionState<AddEntryData> {

    @Override
    public void sendInfo(TelegramClient client, long chatId, AddEntryData data) throws TelegramApiException {
        String text = "Введите идентификатор сдачи";
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        client.execute(message);
    }

    @Override
    public boolean handle(Update update, TelegramClient client, SessionWithState<AddEntryData> session) throws TelegramApiException {
        try {
            int number = Integer.parseInt(update.getMessage().getText());
            session.getData().setBoardNumber(number);
            SendMessage message = SendMessage.builder()
                        .chatId(TelegramUtils.getChatId(update))
                        .text(".")
                        .build();
            session.getData().setMessageId(client.execute(message).getMessageId());
            session.updateState(new ContractAddEntryState());
        } catch (NumberFormatException e) {
            SendMessage message = SendMessage.builder()
                    .chatId(TelegramUtils.getChatId(update))
                    .text("Номер или идентификатор сдачи это целое число. Введите снова")
                    .build();
            client.execute(message);
        }
        return false;
    }
}
