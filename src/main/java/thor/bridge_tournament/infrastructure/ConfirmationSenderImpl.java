package thor.bridge_tournament.infrastructure;

import org.springframework.stereotype.Component;
import thor.bridge_tournament.core.port.output.ConfirmationSender;

import java.util.UUID;

@Component
public class ConfirmationSenderImpl implements ConfirmationSender {
    @Override
    public void oddNumberOfPlayers(UUID userId, String tournamentName) {

    }
}
