package thor.bridge_tournament.presentation.telegram.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.exception.CommandNotFoundException;
import thor.bridge_tournament.presentation.telegram.session.UserSession;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.core.domain.identity.ExternalIdentity;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;
import thor.bridge_tournament.core.port.input.UserIdentityService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MainCommandHandler {
    @Getter
    private final List<CommandHandler> handlers;
    private final UserIdentityService identityService;

    @Value("${bot.username:BridgeTournamentBot}")
    private String botUsername = "BridgeTournamentBot";

    public boolean isAddressedToThisBot(String command) {
        String name = command.strip().split("\\s+", 2)[0];
        int at = name.indexOf('@');
        return at < 0 || name.substring(at + 1).equalsIgnoreCase(botUsername);
    }

    public Optional<UserSession> handle(Update update, TelegramClient telegramClient, String command) throws CommandNotFoundException, TelegramApiException {
        if (!isAddressedToThisBot(command)) {
            return Optional.empty();
        }
        String name = command.strip().split("\\s+", 2)[0].split("@", 2)[0].toLowerCase(Locale.ROOT);
        for (CommandHandler handler: handlers) {
            if (handler.canHandle(name)) {
                var sender = TelegramUtils.getUser(update);
                var userId = identityService.resolveOrRegister(
                        new ExternalIdentity(IdentityProvider.TELEGRAM, sender.getId().toString()), sender.getUserName());
                return handler.handle(update, telegramClient, userId);
            }
        }
        throw new CommandNotFoundException(command);
    }
}
