package thor.bridge_tournament.presentation.telegram.session;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.input.MovementService;
import thor.bridge_tournament.core.port.input.TournamentBoardEntryService;
import thor.bridge_tournament.presentation.html.HtmlProtocolCreator;
import thor.bridge_tournament.presentation.telegram.session.state.entry.AddTournamentEntryBoardNumberState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.util.UUID;

public class AddTournamentEntrySession extends AbstractAddEntrySession {
    private final TournamentBoardEntryService service;

    public AddTournamentEntrySession(Update update, TelegramClient client, TournamentBoardEntryService service,
                                     MovementService movements, HtmlProtocolCreator creator, UUID userId) {
        super(update, client, creator, userId, new AddTournamentEntryBoardNumberState(movements, userId), createData());
        this.service = service;
    }

    private static AddEntryData createData() {
        var data = new AddEntryData();
        data.setBoardLabel("Номер сдачи");
        return data;
    }

    @Override
    protected PairBoardResult saveEntry(RawBoardEntry entry) {
        return service.addTournamentBoardEntry(userId, getData().getBoardNumber(), entry);
    }
}
