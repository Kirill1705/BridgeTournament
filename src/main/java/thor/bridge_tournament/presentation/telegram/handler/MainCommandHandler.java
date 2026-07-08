package thor.bridge_tournament.presentation.telegram.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.exception.CommandNotFoundException;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class MainCommandHandler {
    @Getter
    private final List<CommandHandler> handlers;

    public Optional<UserSession> handle(Update update, TelegramClient telegramClient, String command) throws CommandNotFoundException, TelegramApiException {
        for (CommandHandler handler: handlers) {
            if (handler.canHandle(command)) {
                return handler.handle(update, telegramClient);
            }
        }
        throw new CommandNotFoundException(command);
    }
}
