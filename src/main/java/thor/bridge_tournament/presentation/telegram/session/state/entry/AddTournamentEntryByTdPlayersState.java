package thor.bridge_tournament.presentation.telegram.session.state.entry;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.TournamentMessages;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.util.UUID;

@RequiredArgsConstructor
public class AddTournamentEntryByTdPlayersState implements SessionState<AddEntryData> {
    private final TournamentService tournaments;
    private final UserService users;
    private final UUID tdId;

    @Override
    public void sendInfo(TelegramClient client, long chatId, AddEntryData data) throws TelegramApiException {
        String prompt = data.getFirstPairPlayer() == null
                ? "Судейский ввод или перезапись результата. Введите username любого игрока первой пары."
                : "Первая пара выбрана по игроку " + TournamentMessages.user(data.getFirstPairPlayer())
                + ".\nВведите username любого игрока пары оппонентов.";
        TournamentMessages.send(client, chatId, prompt + "\nМожно с @ или без него. Отмена: /cancel.");
    }

    @Override
    public boolean handle(Update update, TelegramClient client, SessionWithState<AddEntryData> session) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            client.execute(AnswerCallbackQuery.builder().callbackQueryId(update.getCallbackQuery().getId()).build());
            return false;
        }
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        long chatId = TelegramUtils.getChatId(update);
        String username = update.getMessage().getText().strip().replaceFirst("^@+", "");
        var matches = users.findByUsername(IdentityProvider.TELEGRAM, username);
        if (matches.size() != 1) {
            TournamentMessages.send(client, chatId, matches.isEmpty()
                    ? "Пользователь с таким username не найден. Проверьте написание."
                    : "Не удалось однозначно определить пользователя по username.");
            return false;
        }
        var selected = matches.getFirst();
        var pairs = tournaments.getAllPlayers(tdId).pairs();
        var pair = pairs.stream().filter(candidate -> candidate.firstPlayer().id().equals(selected.id())
                || candidate.secondPlayer().id().equals(selected.id())).findFirst();
        if (pair.isEmpty()) {
            TournamentMessages.send(client, chatId, "Этот игрок не состоит в паре вашего текущего турнира.");
            return false;
        }
        var data = session.getData();
        if (data.getFirstPairPlayer() == null) {
            data.setFirstPairPlayer(selected);
        } else {
            UUID first = data.getFirstPairPlayer().id();
            if (pair.get().firstPlayer().id().equals(first) || pair.get().secondPlayer().id().equals(first)) {
                TournamentMessages.send(client, chatId, "Укажите игрока другой пары, а не партнёра первого игрока.");
                return false;
            }
            data.setSecondPairPlayer(selected);
            session.updateState(new AddEntryBoardIdState());
        }
        return false;
    }
}
