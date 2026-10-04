package thor.bridge_tournament.presentation.telegram.session;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.presentation.telegram.session.state.register.NameRegisterState;
import thor.bridge_tournament.presentation.telegram.session.state.register.RegisterStateData;

import java.util.UUID;

public class RegisterSession extends AbstractSessionWithState<RegisterStateData> {
    private final UserService service;
    private final String username;
    private final UUID userId;

    public RegisterSession(Update update, TelegramClient client, UserService service, UUID userId, String username) {
        super(new NameRegisterState(), update, client, new RegisterStateData());
        this.service = service;
        this.username = username;
        this.userId = userId;
    }

    @Override
    protected void finishInteractiveChain(long chatId, TelegramClient client) throws TelegramApiException {
        service.register(new UserDto(userId, username, getData().getName(), getData().getSurname(), getData().getSportCategory()));
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Успешная регистрация")
                .build();
        client.execute(message);
    }
}
