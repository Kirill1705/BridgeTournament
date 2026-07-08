package thor.bridge_tournament.presentation.telegram.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.BoardService;
import thor.bridge_tournament.presentation.telegram.session.AddBoardSession;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AddBoardHandler implements CommandHandler{
    @Getter
    private final String name = "addboard";

    private final BoardService boardService;

    @Override
    public boolean canHandle(String command) {
        return command.equalsIgnoreCase(name);
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient telegramClient) {
        SendMessage message = SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text("Введите номер сдачи")
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        AddBoardSession session = new AddBoardSession(boardService);
        return Optional.of(session);
    }

    @Override
    public String getDescription() {
        return "Добавить сдачу в систему и получить её идентификатор";
    }
}
