package thor.bridge_tournament.presentation.telegram.session;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.input.BoardEntryService;
import thor.bridge_tournament.presentation.html.HtmlProtocolCreator;
import thor.bridge_tournament.presentation.telegram.session.state.entry.AddEntryCountTypeState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.util.UUID;

public class AddEntrySession extends AbstractAddEntrySession {
    private final BoardEntryService service;

    public AddEntrySession(Update update, TelegramClient client, BoardEntryService service, HtmlProtocolCreator protocolCreator, UUID userId) {
        super(update, client, protocolCreator, userId, new AddEntryCountTypeState(), new AddEntryData());
        this.service = service;
    }

    @Override
    protected PairBoardResult saveEntry(RawBoardEntry entry) {
        if (getData().getCountType().equalsIgnoreCase("IMP")) {
            return service.addBoardEntryImps(userId, getData().getBoardNumber(), entry, 0d);
        }
        return service.addBoardEntryMp(userId, getData().getBoardNumber(), entry);
    }
}
