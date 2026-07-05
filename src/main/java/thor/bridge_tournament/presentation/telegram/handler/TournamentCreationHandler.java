package thor.bridge_tournament.presentation.telegram.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.presentation.telegram.session.TournamentCreationSession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;

@Component
@AllArgsConstructor
public class TournamentCreationHandler implements CommandHandler {
    private final TournamentService service;
    @Override
    public boolean canHandle(String command) {
        return command.equals("create");
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient telegramClient) {
        TournamentCreationSession session = new TournamentCreationSession(service, update, telegramClient);
        return Optional.of(session);
    }
}
