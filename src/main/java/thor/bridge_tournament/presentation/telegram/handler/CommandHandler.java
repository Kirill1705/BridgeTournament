package thor.bridge_tournament.presentation.telegram.handler;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;

public interface CommandHandler {
    boolean canHandle(String command);

    Optional<UserSession> handle(Update update, TelegramClient telegramClient) throws TelegramApiException;
}
