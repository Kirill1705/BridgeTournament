package thor.bridge_tournament.presentation.telegram.session;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface UserSession {
    boolean handleMessage(Update update, TelegramClient client);
}
