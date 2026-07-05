package thor.bridge_tournament.presentation.telegram.session.state.tournament;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;

@AllArgsConstructor
public class TournamentCreationNameState implements SessionState<TournamentCreationData> {

    @Override
    public void sendInfo(TelegramClient client, long chatId, TournamentCreationData data) throws TelegramApiException {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Введите название турнира")
                .build();
        client.execute(message);
    }

    @Override
    public boolean handle(Update update, TelegramClient client, SessionWithState<TournamentCreationData> session) {
        String text = update.getMessage().getText().trim();
        session.getData().setName(text);
        session.updateState(new TournamentCreationRoundsState());
        return false;
    }
}
