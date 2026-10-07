package thor.bridge_tournament.presentation.telegram.session;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.TournamentMessages;

import java.util.UUID;

@RequiredArgsConstructor
public class AddTournamentBoardSession implements UserSession {
    private final TournamentService tournaments;
    private final UUID userId;
    private boolean completed;

    @Override
    public boolean handleMessage(Update update, TelegramClient client) {
        if (completed) {
            return true;
        }
        try {
            int boardId;
            try {
                if (!update.hasMessage() || !update.getMessage().hasText()) {
                    throw new NumberFormatException();
                }
                boardId = Integer.parseInt(update.getMessage().getText().strip());
                if (boardId <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                TournamentMessages.send(client, TelegramUtils.getChatId(update), "Введите положительный целочисленный идентификатор сдачи, полученный через /addboard, или /cancel.");
                return false;
            }
            tournaments.addBoardToTournament(boardId, userId);
            completed = true;
            TournamentMessages.send(client, TelegramUtils.getChatId(update), "Сдача с идентификатором " + boardId + " добавлена в текущий турнир.");
            return true;
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
