package thor.bridge_tournament.presentation.telegram.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.MovementService;
import thor.bridge_tournament.presentation.telegram.TelegramFileUtils;
import thor.bridge_tournament.presentation.telegram.session.AddMovementSession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;

@Component
@AllArgsConstructor
public class AddMovementHandler implements CommandHandler {
    private final TelegramFileUtils fileUtils;
    private final MovementService movementService;

    @Override
    public boolean canHandle(String command) {
        return command.equalsIgnoreCase("addmovement");
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient telegramClient) {
        long chatId = update.getMessage().getChatId();
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Отправьте JSON файл с движением")
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        AddMovementSession session = new AddMovementSession(movementService, fileUtils);
        return Optional.of(session);
    }
}
