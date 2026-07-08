package thor.bridge_tournament.presentation.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.exception.CommandNotFoundException;
import thor.bridge_tournament.presentation.telegram.exception.ExceptionHandler;
import thor.bridge_tournament.presentation.telegram.handler.MainCommandHandler;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelegramBotMainClass implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final String botToken;
    private final TelegramClient telegramClient;

    private final MainCommandHandler handler;

    private final ExceptionHandler exceptionHandler;

    private final Map<Long, UserSession> sessions = new ConcurrentHashMap<>();

    public TelegramBotMainClass(@Value("${bot.token}") String botToken, MainCommandHandler handler, ExceptionHandler exceptionHandler) {
        this.handler = handler;
        this.botToken = botToken;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.exceptionHandler = exceptionHandler;
        initBotCommands();
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        Optional<String> text = getText(update);
        long chatId = TelegramUtils.getChatId(update);
        if (text.isPresent() && text.get().startsWith("/")) {
            try {
                Optional<UserSession> session = handler.handle(update, telegramClient, text.get().substring(1));
                session.ifPresent(userSession -> sessions.put(chatId, userSession));
            } catch (CommandNotFoundException e) {
                sendUnknownCommandMessage(update.getMessage().getChatId());
            }
            catch (Exception e) {
                sendMessage(chatId, exceptionHandler.handle(e));
                throw new RuntimeException(e);
            }
        }
        else if (sessions.containsKey(chatId)) {
            UserSession session = sessions.get(chatId);
            try {
                if (session.handleMessage(update, telegramClient)) {
                    sessions.remove(chatId);
                }
            } catch (Exception e) {
                sendMessage(chatId, exceptionHandler.handle(e));
                throw new RuntimeException(e);
            }
        }
        else {
            sendSessionNotFoundMessage(chatId);
        }
    }

    private void initBotCommands() {
        List<BotCommand> commands = handler.getHandlers().stream()
                .map(commandHandler -> new BotCommand(commandHandler.getName(), commandHandler.getDescription()))
                .toList();
        try {
            DeleteMyCommands deleteMyCommands = DeleteMyCommands.builder()
                            .scope(new BotCommandScopeDefault())
                            .build();
            telegramClient.execute(deleteMyCommands);
            SetMyCommands setMyCommands = SetMyCommands.builder()
                    .commands(commands)
                    .scope(new BotCommandScopeDefault())
                    .build();
            telegramClient.execute(setMyCommands);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<String> getText(Update update) {
        if (update.hasMessage() && update.getMessage().getText() != null) {
            return Optional.of(update.getMessage().getText().trim());
        }
        return Optional.empty();
    }

    private void sendUnknownCommandMessage(long charId) {
        SendMessage message = SendMessage.builder()
                .chatId(charId)
                .text("Такой команды не существует (пока что)")
                .build();
        executeMessage(message);
    }

    private void sendSessionNotFoundMessage(long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Сначала используете команду (напишите /имя_команды)")
                .build();
        executeMessage(message);
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
