package thor.bridge_tournament.presentation.telegram.session;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;

public abstract class AbstractSessionWithState<T> implements SessionWithState<T> {
    @Getter
    private final T data;

    private SessionState<T> state;

    public AbstractSessionWithState(SessionState<T> state, Update update, TelegramClient client, T data) {
        this.state = state;
        this.data = data;
        try {
            state.sendInfo(client, TelegramUtils.getChatId(update), data);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateState(SessionState<T> state) {
        this.state = state;
    }

    @Override
    public boolean handleMessage(Update update, TelegramClient client) {
        try {
            boolean ready = state.handle(update, client, this);
            if (ready) {
                finishInteractiveChain(TelegramUtils.getChatId(update), client);
                return true;
            }
            state.sendInfo(client, TelegramUtils.getChatId(update), data);
        } catch (TelegramApiException _) {

        }
        return false;
    }

    protected abstract void finishInteractiveChain(long chatId, TelegramClient client) throws TelegramApiException;
}
