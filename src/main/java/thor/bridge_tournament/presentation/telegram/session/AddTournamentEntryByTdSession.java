package thor.bridge_tournament.presentation.telegram.session;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.input.TournamentBoardEntryService;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.presentation.html.HtmlProtocolCreator;
import thor.bridge_tournament.presentation.telegram.session.state.entry.AddTournamentEntryByTdPlayersState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.util.UUID;

public class AddTournamentEntryByTdSession extends AbstractAddEntrySession {
    private final TournamentBoardEntryService service;

    public AddTournamentEntryByTdSession(Update update, TelegramClient client, TournamentBoardEntryService service,
                                         TournamentService tournaments, UserService users, HtmlProtocolCreator creator, UUID tdId) {
        super(update, client, creator, tdId, new AddTournamentEntryByTdPlayersState(tournaments, users, tdId), createData());
        this.service = service;
    }

    private static AddEntryData createData() {
        var data = new AddEntryData();
        data.setBoardLabel("Номер сдачи");
        return data;
    }

    @Override
    protected PairBoardResult saveEntry(RawBoardEntry entry) {
        var data = getData();
        return service.addTournamentBoardEntryByTd(userId, data.getBoardNumber(), entry,
                data.getFirstPairPlayer().id(), data.getSecondPairPlayer().id());
    }
}
