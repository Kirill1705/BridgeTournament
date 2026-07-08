package thor.bridge_tournament.presentation.telegram.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.presentation.telegram.session.RegisterSession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RegisterCommandHandler implements CommandHandler{
    @Getter
    private final String name = "register";

    private final UserService service;

    @Override
    public boolean canHandle(String command) {
        return command.equals(name);
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient telegramClient) {
        RegisterSession session = new RegisterSession(update, telegramClient, service);
        return Optional.of(session);
    }

    @Override
    public String getDescription() {
        return "Зарегестрироваться в системе";
    }
}
