package thor.bridge_tournament.presentation.telegram.session;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.exception.DomainValidationException;
import thor.bridge_tournament.core.exception.TournamentNotFoundException;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.TournamentMessages;
import thor.bridge_tournament.presentation.telegram.session.state.tournament.TournamentUserSelectionData;
import thor.bridge_tournament.presentation.telegram.session.state.tournament.TournamentUserSelectionData.Action;
import thor.bridge_tournament.presentation.telegram.session.state.tournament.TournamentUserSelectionState;
import thor.bridge_tournament.presentation.telegram.session.state.tournament.TournamentRegistrationState;
import thor.bridge_tournament.presentation.telegram.session.state.tournament.TournamentPairManagementState;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TournamentUserSelectionSession extends AbstractSessionWithState<TournamentUserSelectionData> {
    private final TournamentService tournaments;
    private final UUID userId;
    private boolean applied;
    private List<UserDto> removedUsers = List.of();

    private TournamentUserSelectionSession(TournamentService tournaments, UserService users, UUID userId, Update update,
                                           TelegramClient client, TournamentUserSelectionData data) {
        super(switch (data.getAction()) {
            case DIRECTOR -> new TournamentUserSelectionState();
            case PLAYER, PAIR -> new TournamentRegistrationState(tournaments, users);
            case DIRECTOR_PAIR, REMOVE_PLAYER -> new TournamentPairManagementState(tournaments, users, userId);
        }, update, client, data);
        this.tournaments = tournaments;
        this.userId = userId;
    }

    public static Optional<UserSession> open(TournamentService tournaments, UserService users, UUID userId,
                                              Action action, Update update, TelegramClient client) throws TelegramApiException {
        if (action == Action.PLAYER || action == Action.PAIR) {
            var initiator = users.getAllPlayers().stream().filter(user -> user.id().equals(userId)).findFirst().orElseThrow();
            var data = new TournamentUserSelectionData(action, List.of());
            data.getSelected().add(initiator);
            return Optional.of(new TournamentUserSelectionSession(tournaments, users, userId, update, client, data));
        }
        if (action == Action.DIRECTOR_PAIR || action == Action.REMOVE_PLAYER) {
            try {
                tournaments.getTds(userId);
            } catch (TournamentNotFoundException e) {
                TournamentMessages.send(client, TelegramUtils.getChatId(update),
                        "Эта команда доступна создателю или судье своего текущего турнира.");
                return Optional.empty();
            }
            return Optional.of(new TournamentUserSelectionSession(tournaments, users, userId, update, client,
                    new TournamentUserSelectionData(action, List.of())));
        }
        var excluded = new HashSet<UUID>();
        tournaments.getTds(userId).stream().map(UserDto::id).forEach(excluded::add);
        var candidates = TournamentMessages.sortedUsers(users.getAllPlayers().stream()
                .filter(user -> !excluded.contains(user.id())).toList());
        var data = new TournamentUserSelectionData(action, candidates);
        if (candidates.size() < data.requiredUsers()) {
            TournamentMessages.send(client, TelegramUtils.getChatId(update),
                    "Нет пользователей, которых можно добавить. Профиль можно заполнить через /register.");
            return Optional.empty();
        }
        return Optional.of(new TournamentUserSelectionSession(tournaments, users, userId, update, client, data));
    }

    @Override
    protected void finishInteractiveChain(long chatId, TelegramClient client) throws TelegramApiException {
        var data = getData();
        if (data.isCancelled()) {
            TournamentMessages.send(client, chatId, "Действие отменено.");
            return;
        }
        var first = data.getSelected().getFirst();
        if (!applied) {
            try {
                switch (data.getAction()) {
                    case DIRECTOR -> tournaments.addTournamentDirector(userId, first.id());
                    case PLAYER -> tournaments.addPlayer(data.getDirector().id(), userId);
                    case PAIR -> tournaments.addPair(data.getDirector().id(), userId, data.getSelected().getLast().id());
                    case DIRECTOR_PAIR -> tournaments.addPair(userId, first.id(), data.getSelected().getLast().id());
                    case REMOVE_PLAYER -> removedUsers = tournaments.removePlayer(userId, first.id());
                }
            } catch (DomainValidationException e) {
                if (data.getAction() == Action.DIRECTOR_PAIR || data.getAction() == Action.REMOVE_PLAYER) {
                    data.getSelected().clear();
                }
                throw e;
            }
            // Retrying a failed Telegram reply must not repeat the database operation.
            applied = true;
        }
        String message = switch (data.getAction()) {
            case DIRECTOR -> "Судья добавлен: " + TournamentMessages.user(first);
            case PLAYER -> "Вы записаны без пары в турнир судьи " + TournamentMessages.user(data.getDirector()) + ".";
            case PAIR -> "Вы и " + TournamentMessages.user(data.getSelected().getLast())
                    + " записаны парой в турнир судьи " + TournamentMessages.user(data.getDirector()) + ".";
            case DIRECTOR_PAIR -> "Пара добавлена в ваш турнир: " + TournamentMessages.user(first)
                    + " — " + TournamentMessages.user(data.getSelected().getLast());
            case REMOVE_PLAYER -> (removedUsers.size() == 1 ? "Из турнира удалён игрок без пары: " : "Из турнира удалена пара: ")
                    + removedUsers.stream().map(TournamentMessages::user).collect(Collectors.joining(" — "));
        };
        TournamentMessages.send(client, chatId, message);
    }
}
