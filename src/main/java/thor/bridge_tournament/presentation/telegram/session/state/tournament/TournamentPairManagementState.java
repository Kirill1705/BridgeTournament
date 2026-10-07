package thor.bridge_tournament.presentation.telegram.session.state.tournament;

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
import thor.bridge_tournament.presentation.telegram.session.state.tournament.TournamentUserSelectionData.Action;

import java.util.UUID;

@RequiredArgsConstructor
public class TournamentPairManagementState implements SessionState<TournamentUserSelectionData> {
    private final TournamentService tournaments;
    private final UserService users;
    private final UUID directorId;

    @Override
    public void sendInfo(TelegramClient client, long chatId, TournamentUserSelectionData data) throws TelegramApiException {
        String prompt = switch (data.getAction()) {
            case DIRECTOR_PAIR -> data.getSelected().isEmpty() ? "Введите username первого игрока пары для вашего турнира."
                    : "Первый игрок: " + TournamentMessages.user(data.getSelected().getFirst()) + ".\nВведите username второго игрока.";
            case REMOVE_PLAYER -> "Введите username участника вашего турнира. Игрок без пары будет удалён один; если он в паре, будут удалены оба игрока пары.";
            default -> throw new IllegalStateException("Unsupported tournament management action");
        };
        TournamentMessages.send(client, chatId, prompt + "\nМожно с @ или без него. Отмена: /cancel.");
    }

    @Override
    public boolean handle(Update update, TelegramClient client, SessionWithState<TournamentUserSelectionData> session) throws TelegramApiException {
        var data = session.getData();
        if (update.hasCallbackQuery()) {
            client.execute(AnswerCallbackQuery.builder().callbackQueryId(update.getCallbackQuery().getId()).build());
        }
        if (data.isComplete()) {
            return true;
        }
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        long chatId = TelegramUtils.getChatId(update);
        String username = update.getMessage().getText().strip().replaceFirst("^@", "");
        var matches = users.findByUsername(IdentityProvider.TELEGRAM, username);
        if (matches.isEmpty()) {
            TournamentMessages.send(client, chatId, "Пользователь с таким username не найден. Проверьте написание и попросите его обратиться к боту.");
            return false;
        }
        if (matches.size() != 1) {
            TournamentMessages.send(client, chatId, "Не удалось однозначно определить пользователя по username.");
            return false;
        }
        var selected = matches.getFirst();
        var participants = tournaments.getAllPlayers(directorId);
        boolean paired = participants.pairs().stream().anyMatch(pair -> pair.firstPlayer().id().equals(selected.id())
                || pair.secondPlayer().id().equals(selected.id()));
        if (data.getAction() == Action.DIRECTOR_PAIR) {
            if (data.getSelected().stream().anyMatch(user -> user.id().equals(selected.id()))) {
                TournamentMessages.send(client, chatId, "В паре должны быть два разных игрока. Укажите второго игрока.");
                return false;
            }
            if (paired) {
                TournamentMessages.send(client, chatId, "Этот игрок уже состоит в паре вашего турнира. Укажите другого игрока.");
                return false;
            }
        } else if (!paired && participants.playersWithOutPair().stream().noneMatch(user -> user.id().equals(selected.id()))) {
            TournamentMessages.send(client, chatId, "Этот пользователь не участвует в вашем текущем турнире.");
            return false;
        }
        data.getSelected().add(selected);
        return data.isComplete();
    }
}
