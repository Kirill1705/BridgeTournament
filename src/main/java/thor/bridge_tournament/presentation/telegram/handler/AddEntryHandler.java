package thor.bridge_tournament.presentation.telegram.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.MovementService;
import thor.bridge_tournament.presentation.html.HtmlProtocolCreator;
import thor.bridge_tournament.presentation.telegram.TelegramFileUtils;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.session.AddEntrySession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;

@Component
@AllArgsConstructor
public class AddEntryHandler implements CommandHandler {
    private final MovementService service;
    private final HtmlProtocolCreator creator;

    @Override
    public boolean canHandle(String command) {
        return command.equalsIgnoreCase("addentry");
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient telegramClient) {
        AddEntrySession session = new AddEntrySession(update, telegramClient, false, service, creator, update.getMessage().getFrom().getUserName());
        return Optional.of(session);
    }
}
