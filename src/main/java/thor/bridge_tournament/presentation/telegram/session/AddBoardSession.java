package thor.bridge_tournament.presentation.telegram.session;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.BoardService;

@AllArgsConstructor
public class AddBoardSession implements UserSession {
    private final BoardService boardService;

    @Override
    public boolean handleMessage(Update update, TelegramClient client) {
        try {
            int boardNumber = Integer.parseInt(update.getMessage().getText());
            int id = boardService.addBoard(boardNumber);
            SendMessage message = SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Идентификатор сдачи: " + id + ". Его используют для записи результата")
                    .build();
            client.execute(message);
            return true;
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
