package thor.bridge_tournament.presentation.telegram.session.state.entry;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.MovementService;
import thor.bridge_tournament.presentation.telegram.TournamentMessages;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AddTournamentEntryBoardNumberState extends AddEntryBoardIdState {
    private final MovementService movements;
    private final UUID userId;

    @Override
    public void sendInfo(TelegramClient client, long chatId, AddEntryData data) throws TelegramApiException {
        var next = movements.getMovementNextRound(userId);
        var meeting = next.movementEntry();
        String opponents = meeting.opponents() == null ? "нет"
                : TournamentMessages.user(meeting.opponents().firstPlayer()) + " — " + TournamentMessages.user(meeting.opponents().secondPlayer());
        String boards = next.dealsNotPlayed().stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
        TournamentMessages.send(client, chatId, "Тур " + meeting.round() + ", стол " + meeting.table()
                + ".\nСоперники: " + opponents + ".\nДоступные сдачи: " + boards
                + ".\nВведите номер сдачи из этого списка. Отмена: /cancel.");
    }

    @Override
    protected boolean validateBoard(int number, TelegramClient client, long chatId) throws TelegramApiException {
        if (!movements.getMovementNextRound(userId).dealsNotPlayed().contains(number)) {
            TournamentMessages.send(client, chatId, "Этой сдачи нет среди незаписанных сдач текущего тура. Выберите номер из списка.");
            return false;
        }
        return true;
    }
}
