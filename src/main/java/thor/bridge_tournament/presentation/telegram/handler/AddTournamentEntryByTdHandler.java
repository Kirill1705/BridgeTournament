package thor.bridge_tournament.presentation.telegram.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.exception.DomainValidationException;
import thor.bridge_tournament.core.port.input.TournamentBoardEntryService;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.presentation.html.HtmlProtocolCreator;
import thor.bridge_tournament.presentation.telegram.session.AddTournamentEntryByTdSession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AddTournamentEntryByTdHandler implements CommandHandler {
    @Getter
    private final String name = "addtournamententrybytd";
    private final TournamentBoardEntryService service;
    private final TournamentService tournaments;
    private final UserService users;
    private final HtmlProtocolCreator creator;

    @Override
    public boolean canHandle(String command) {
        return name.equalsIgnoreCase(command);
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient client, UUID userId) {
        if (tournaments.getTds(userId).stream().noneMatch(td -> td.id().equals(userId))) {
            throw new DomainValidationException("Эта команда доступна только судье текущего турнира");
        }
        return Optional.of(new AddTournamentEntryByTdSession(update, client, service, tournaments, users, creator, userId));
    }

    @Override
    public String getDescription() {
        return "Судье ввести или перезаписать результат встречи двух пар";
    }
}
