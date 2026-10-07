package thor.bridge_tournament.presentation.telegram.session.state.tournament;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.exception.TournamentNotFoundException;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;
import thor.bridge_tournament.core.port.dto.tournament.TournamentPlayers;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.TournamentMessages;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;
import thor.bridge_tournament.presentation.telegram.session.state.tournament.TournamentUserSelectionData.Action;

import java.util.UUID;

@RequiredArgsConstructor
public class TournamentRegistrationState implements SessionState<TournamentUserSelectionData> {
    private final TournamentService tournaments;
    private final UserService users;

    @Override
    public void sendInfo(TelegramClient client, long chatId, TournamentUserSelectionData data) throws TelegramApiException {
        String prompt = data.getDirector() == null
                ? "Введите username создателя или судьи турнира, в который хотите записаться."
                : "Судья: " + TournamentMessages.user(data.getDirector()) + ".\nВведите username партнёра. Первым игроком пары будете вы.";
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
            TournamentMessages.send(client, chatId, "Пользователь с таким username не найден. Проверьте написание и попросите его обратиться к боту, чтобы обновить профиль.");
            return false;
        }
        if (matches.size() != 1) {
            TournamentMessages.send(client, chatId, "Этот username сохранён у нескольких пользователей. Не удалось однозначно определить пользователя; владельцам профилей нужно обратиться к боту, чтобы обновить username.");
            return false;
        }
        var selected = matches.getFirst();
        UUID initiatorId = data.getSelected().getFirst().id();
        boolean selectingDirector = data.getDirector() == null;
        if (!selectingDirector && selected.id().equals(initiatorId)) {
            TournamentMessages.send(client, chatId, "Нельзя составить пару с самим собой. Укажите username партнёра.");
            return false;
        }
        TournamentPlayers participants;
        try {
            participants = tournaments.getAllPlayers(selectingDirector ? selected.id() : data.getDirector().id());
        } catch (TournamentNotFoundException e) {
            data.setDirector(null);
            TournamentMessages.send(client, chatId, "У указанного пользователя нет текущего турнира в роли судьи. Укажите username другого судьи или создателя турнира.");
            return false;
        }
        if (isPaired(participants, initiatorId)) {
            TournamentMessages.send(client, chatId, "Вы уже состоите в паре этого турнира. Повторная запись не нужна.");
            return false;
        }
        if (data.getAction() == Action.PLAYER && participants.playersWithOutPair().stream().anyMatch(user -> user.id().equals(initiatorId))) {
            TournamentMessages.send(client, chatId, "Вы уже записаны в этот турнир без пары. Чтобы записаться с партнёром, используйте /addpair.");
            return false;
        }
        if (selectingDirector) {
            data.setDirector(selected);
        } else {
            if (isPaired(participants, selected.id())) {
                TournamentMessages.send(client, chatId, "Этот партнёр уже состоит в паре турнира. Укажите другого партнёра.");
                return false;
            }
            data.getSelected().add(selected);
        }
        return data.isComplete();
    }

    private boolean isPaired(TournamentPlayers participants, UUID userId) {
        return participants.pairs().stream().anyMatch(pair -> pair.firstPlayer().id().equals(userId)
                || pair.secondPlayer().id().equals(userId));
    }
}
