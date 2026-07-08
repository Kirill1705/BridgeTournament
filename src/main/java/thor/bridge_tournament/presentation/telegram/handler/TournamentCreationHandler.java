package thor.bridge_tournament.presentation.telegram.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.presentation.telegram.session.TournamentCreationSession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TournamentCreationHandler implements CommandHandler {
    @Getter
    private final String name = "create";

    private final TournamentService service;
    @Override
    public boolean canHandle(String command) {
        return command.equals(name);
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient telegramClient) {
        TournamentCreationSession session = new TournamentCreationSession(service, update, telegramClient);
        return Optional.of(session);
    }

    @Override
    public String getDescription() {
        return "Создать новый турнир";
    }
}
