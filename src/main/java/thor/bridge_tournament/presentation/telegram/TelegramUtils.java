package thor.bridge_tournament.presentation.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramUtils {
    public static long getChatId(Update update) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getMessage() != null) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        else if (update.hasMessage() && update.getMessage() != null) {
            return update.getMessage().getChatId();
        }
        throw new RuntimeException("Cant get chat id");
    }
}
