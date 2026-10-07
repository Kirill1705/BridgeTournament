package thor.bridge_tournament.presentation.telegram.session.state.tournament;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import thor.bridge_tournament.core.port.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class TournamentUserSelectionData {
    public enum Action { DIRECTOR, PLAYER, PAIR, DIRECTOR_PAIR, REMOVE_PLAYER }

    private final Action action;
    private final List<UserDto> candidates;
    private final List<UserDto> selected = new ArrayList<>();
    @Setter
    private UserDto director;
    @Setter
    private boolean cancelled;

    public int requiredUsers() {
        return action == Action.PAIR || action == Action.DIRECTOR_PAIR ? 2 : 1;
    }

    public boolean isComplete() {
        return cancelled || (selected.size() == requiredUsers()
                && (action != Action.PLAYER && action != Action.PAIR || director != null));
    }

    public List<UserDto> remaining() {
        return candidates.stream().filter(candidate -> selected.stream()
                .noneMatch(user -> user.id().equals(candidate.id()))).toList();
    }
}
