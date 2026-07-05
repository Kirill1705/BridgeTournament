package thor.bridge_tournament.presentation.telegram.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.presentation.telegram.session.RegisterSession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;

@Component
@AllArgsConstructor
public class RegisterCommandHandler implements CommandHandler{
    private final UserService service;

    @Override
    public boolean canHandle(String command) {
        return command.equals("register");
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient telegramClient) {
        RegisterSession session = new RegisterSession(update, telegramClient, service);
        return Optional.of(session);
    }
}
