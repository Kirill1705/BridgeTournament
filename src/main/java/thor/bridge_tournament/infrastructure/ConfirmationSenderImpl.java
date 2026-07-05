package thor.bridge_tournament.infrastructure;

import org.springframework.stereotype.Component;
import thor.bridge_tournament.core.port.output.ConfirmationSender;

@Component
public class ConfirmationSenderImpl implements ConfirmationSender {
    @Override
    public void oddNumberOfPlayers(String userName, String tournamentName) {

    }
}
