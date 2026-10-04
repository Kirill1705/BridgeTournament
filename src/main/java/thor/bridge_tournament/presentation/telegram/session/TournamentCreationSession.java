package thor.bridge_tournament.presentation.telegram.session;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.presentation.telegram.session.state.tournament.TournamentCreationCountTypeState;
import thor.bridge_tournament.presentation.telegram.session.state.tournament.TournamentCreationData;
import thor.bridge_tournament.presentation.telegram.session.state.tournament.TournamentCreationRoundsState;

import java.util.UUID;

public class TournamentCreationSession extends AbstractSessionWithState<TournamentCreationData> {
    private final TournamentService tournamentService;
    private final UUID userId;

    public TournamentCreationSession(TournamentService tournamentService, Update update, TelegramClient client, UUID userId) {
        super(new TournamentCreationCountTypeState(), update, client, new TournamentCreationData());
        this.tournamentService = tournamentService;
        this.userId = userId;
    }

    @Override
    public void finishInteractiveChain(long chatId, TelegramClient client) throws TelegramApiException {
        tournamentService.createTournament(userId, getData().getBoards(), getData().getCountType(), getData().getName(), getData().getRounds());
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Турнир создан успешно")
                .build();
        client.execute(message);
    }
}
