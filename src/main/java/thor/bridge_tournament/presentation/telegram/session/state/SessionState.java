package thor.bridge_tournament.presentation.telegram.session.state;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

public interface SessionState<T> {
    void sendInfo(TelegramClient client, long chatId, T data) throws TelegramApiException;

    boolean handle(Update update, TelegramClient client, SessionWithState<T> session) throws TelegramApiException;
}
