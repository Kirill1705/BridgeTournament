package thor.bridge_tournament.presentation.telegram.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.presentation.telegram.session.StartTournamentSession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StartTournamentHandler implements CommandHandler {
    @Getter
    private final String name = "starttournament";
    private final TournamentService tournaments;

    @Override
    public boolean canHandle(String command) {
        return name.equalsIgnoreCase(command);
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient client, UUID userId) throws TelegramApiException {
        return Optional.of(new StartTournamentSession(tournaments, userId, update, client));
    }

    @Override
    public String getDescription() {
        return "Начать текущий турнир и сформировать расписание";
    }
}
