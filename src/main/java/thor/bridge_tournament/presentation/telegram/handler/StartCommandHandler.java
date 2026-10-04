package thor.bridge_tournament.presentation.telegram.handler;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.Optional;
import java.util.UUID;

@Component
public class StartCommandHandler implements CommandHandler{
    @Getter
    private final String name = "start";

    @Override
    public boolean canHandle(String command) {
        return command.equalsIgnoreCase(name);
    }

    @Override
    public Optional<UserSession> handle(Update update, TelegramClient telegramClient, UUID userId) throws TelegramApiException {
        SendMessage message = SendMessage.builder()
                .chatId(TelegramUtils.getChatId(update))
                .text("Это бот для проведения турниров и ведения протокола для спортивного Бриджа")
                .build();
        telegramClient.execute(message);
        return Optional.empty();
    }

    @Override
    public String getDescription() {
        return "Познакомиться с ботом";
    }
}
