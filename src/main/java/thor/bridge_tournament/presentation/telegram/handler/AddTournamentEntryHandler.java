package thor.bridge_tournament.presentation.telegram.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.MovementService;
import thor.bridge_tournament.core.port.input.TournamentBoardEntryService;
import thor.bridge_tournament.presentation.html.HtmlProtocolCreator;
import thor.bridge_tournament.presentation.telegram.session.AddTournamentEntrySession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AddTournamentEntryHandler implements CommandHandler {
    @Getter
    private final String name = "addtournamententry";
    private final TournamentBoardEntryService service;
    private final MovementService movements;
    private final HtmlProtocolCreator creator;

    @Override
    public boolean canHandle(String command) {
        return name.equalsIgnoreCase(command);
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient client, UUID userId) {
        return Optional.of(new AddTournamentEntrySession(update, client, service, movements, creator, userId));
    }

    @Override
    public String getDescription() {
        return "Ввести результат сдачи текущего турнира по номеру";
    }
}
