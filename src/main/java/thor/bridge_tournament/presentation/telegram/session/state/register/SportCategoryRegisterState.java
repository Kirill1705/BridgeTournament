package thor.bridge_tournament.presentation.telegram.session.state.register;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;

@AllArgsConstructor
public class SportCategoryRegisterState implements SessionState<RegisterStateData> {

    @Override
    public void sendInfo(TelegramClient client, long chatId, RegisterStateData data) throws TelegramApiException {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Введите ваш спортивный разряд. Если не хотите напишите -")
                .build();
        client.execute(message);
    }

    @Override
    public boolean handle(Update update, TelegramClient client, SessionWithState<RegisterStateData> session) throws TelegramApiException {
        String text = update.getMessage().getText().trim();
        try {
            session.getData().setSportCategory(Double.parseDouble(text));
        } catch (NumberFormatException _) {
            SendMessage message = SendMessage.builder()
                    .chatId(TelegramUtils.getChatId(update))
                    .text("Требуется ввести число")
                    .build();
            client.execute(message);
        }
        return true;
    }
}
