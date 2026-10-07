package thor.bridge_tournament.presentation.telegram.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.TournamentMessages;
import thor.bridge_tournament.presentation.telegram.session.AddTournamentBoardSession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AddTournamentBoardHandler implements CommandHandler {
    @Getter
    private final String name = "addtournamentboard";
    private final TournamentService tournaments;

    @Override
    public boolean canHandle(String command) {
        return name.equalsIgnoreCase(command);
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient client, UUID userId) throws TelegramApiException {
        tournaments.getTds(userId); // Check that the caller has a current tournament before asking for input.
        TournamentMessages.send(client, TelegramUtils.getChatId(update),
                "Введите идентификатор существующей сдачи из /addboard. Нужен именно идентификатор в системе, а не номер сдачи. Отмена: /cancel.");
        return Optional.of(new AddTournamentBoardSession(tournaments, userId));
    }

    @Override
    public String getDescription() {
        return "Добавить существующую сдачу в текущий турнир";
    }
}
