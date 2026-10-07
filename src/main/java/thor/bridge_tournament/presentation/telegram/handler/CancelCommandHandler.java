package thor.bridge_tournament.presentation.telegram.handler;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;
import java.util.UUID;

@Component
public class CancelCommandHandler implements CommandHandler {
    @Getter
    private final String name = "cancel";

    @Override
    public boolean canHandle(String command) {
        return name.equalsIgnoreCase(command);
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient client, UUID userId) throws TelegramApiException {
        client.execute(SendMessage.builder().chatId(TelegramUtils.getChatId(update))
                .text("Текущий диалог завершён. Выберите новую команду: /help.")
                .replyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build()).build());
        return Optional.empty();
    }

    @Override
    public String getDescription() {
        return "Отменить текущий диалог";
    }
}
