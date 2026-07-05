package thor.bridge_tournament.core.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class TournamentNotFoundException extends RuntimeException {
    private final UUID userId;

    public TournamentNotFoundException(UUID userId) {
        super("No tournament found with member " + userId);
        this.userId = userId;
    }
}
