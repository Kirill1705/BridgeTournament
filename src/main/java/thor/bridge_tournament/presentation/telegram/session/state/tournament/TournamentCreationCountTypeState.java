package thor.bridge_tournament.presentation.telegram.session.state.tournament;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;

@AllArgsConstructor
public class TournamentCreationCountTypeState implements SessionState<TournamentCreationData> {

    @Override
    public void sendInfo(TelegramClient client, long chatId, TournamentCreationData data) throws TelegramApiException {
        ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(new KeyboardRow("IMP", "MP"))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Выберете способ подсчёта")
                .replyMarkup(keyboard)
                .build();
        client.execute(message);
    }

    @Override
    public boolean handle(Update update, TelegramClient client, SessionWithState<TournamentCreationData> session) {
        String text = update.getMessage().getText();
        session.getData().setCountType(text);
        session.updateState(new TournamentCreationBoardsState());
        return false;
    }
}
