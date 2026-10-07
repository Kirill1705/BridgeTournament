package thor.bridge_tournament.presentation.telegram.session;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.TournamentMessages;

import java.util.UUID;

public class StartTournamentSession implements UserSession {
    private final TournamentService tournaments;
    private final UUID userId;
    private final String token = UUID.randomUUID().toString().substring(0, 8) + ":";
    private boolean completed;

    public StartTournamentSession(TournamentService tournaments, UUID userId, Update update, TelegramClient client) throws TelegramApiException {
        this.tournaments = tournaments;
        this.userId = userId;
        var players = tournaments.getAllPlayers(userId);
        String warning = players.playersWithOutPair().size() % 2 == 0 ? ""
                : "\n⚠ Игроков без пары нечётное количество: один из них не попадёт в расписание.";
        var keyboard = InlineKeyboardMarkup.builder().keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("Начать турнир").callbackData(token + "start").build(),
                InlineKeyboardButton.builder().text("Отмена").callbackData(token + "cancel").build())).build();
        client.execute(SendMessage.builder().chatId(TelegramUtils.getChatId(update))
                .text("Начать текущий турнир?\nГотовых пар: " + players.pairs().size()
                        + ". Игроков без пары: " + players.playersWithOutPair().size()
                        + ".\nИгроки без пары будут объединены автоматически." + warning)
                .replyMarkup(keyboard).build());
    }

    @Override
    public boolean handleMessage(Update update, TelegramClient client) {
        try {
            if (update.hasCallbackQuery()) {
                client.execute(AnswerCallbackQuery.builder().callbackQueryId(update.getCallbackQuery().getId()).build());
            }
            if (completed) {
                return true;
            }
            String action = update.hasCallbackQuery() ? update.getCallbackQuery().getData() : null;
            if ((token + "cancel").equals(action)) {
                completed = true;
                TournamentMessages.send(client, TelegramUtils.getChatId(update), "Запуск турнира отменён.");
                return true;
            }
            if (!(token + "start").equals(action)) {
                TournamentMessages.send(client, TelegramUtils.getChatId(update), "Используйте кнопки подтверждения последней команды /starttournament или /cancel.");
                return false;
            }
            tournaments.startTournament(userId);
            completed = true;
            TournamentMessages.send(client, TelegramUtils.getChatId(update), "Турнир начат. Пары и расписание сформированы.");
            return true;
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
