package thor.bridge_tournament.presentation.telegram.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.presentation.telegram.session.TournamentUserSelectionSession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;
import thor.bridge_tournament.presentation.telegram.session.state.tournament.TournamentUserSelectionData.Action;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AddTournamentPairByDirectorHandler implements CommandHandler {
    @Getter
    private final String name = "addtournamentpair";
    private final TournamentService tournaments;
    private final UserService users;

    @Override
    public boolean canHandle(String command) {
        return name.equalsIgnoreCase(command);
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient client, UUID userId) throws TelegramApiException {
        return TournamentUserSelectionSession.open(tournaments, users, userId, Action.DIRECTOR_PAIR, update, client);
    }

    @Override
    public String getDescription() {
        return "Добавить пару по двум username в свой турнир (для судьи)";
    }
}
