package thor.bridge_tournament.core.port.output;

import java.util.UUID;

public interface ConfirmationSender {
    void oddNumberOfPlayers(UUID userId, String tournamentName);
}
