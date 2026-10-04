package thor.bridge_tournament.presentation.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public class TelegramUtils {
    public static User getUser(Update update) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getFrom() != null) {
            return update.getCallbackQuery().getFrom();
        }
        if (update.hasMessage() && update.getMessage().getFrom() != null) {
            return update.getMessage().getFrom();
        }
        throw new IllegalArgumentException("Update has no user");
    }

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
