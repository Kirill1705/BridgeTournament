package thor.bridge_tournament.presentation.telegram.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HelpCommandHandler implements CommandHandler {
    @Getter
    private final String name = "help";

    private final List<CommandHandler> commands;

    @Override
    public boolean canHandle(String command) {
        return command.equalsIgnoreCase(name);
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient telegramClient) throws TelegramApiException {
        StringBuilder builder = new StringBuilder();
        for (CommandHandler command : commands) {
            builder.append("/").append(command.getName()).append(" - ").append(command.getDescription()).append("\n");
        }
        SendMessage message = SendMessage.builder()
                .chatId(TelegramUtils.getChatId(update))
                .text(builder.toString())
                .build();
        telegramClient.execute(message);
        return Optional.empty();
    }

    @Override
    public String getDescription() {
        return "Вывести список доступных команд";
    }
}
