package thor.bridge_tournament.presentation.telegram.session;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.input.MovementService;
import thor.bridge_tournament.presentation.html.HtmlProtocolCreator;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.session.state.entry.AddEntryBoardNumberState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.io.File;

public class AddEntrySession extends AbstractSessionWithState<AddEntryData> {
    private final MovementService service;
    private final HtmlProtocolCreator protocolCreator;
    private final String username;

    public AddEntrySession(Update update, TelegramClient client, boolean isTournament, MovementService service, HtmlProtocolCreator protocolCreator, String username) {
        super(new AddEntryBoardNumberState(), update, client, new AddEntryData(isTournament));
        this.service = service;
        this.protocolCreator = protocolCreator;
        this.username = username;
    }

    @Override
    protected void finishInteractiveChain(long chatId, TelegramClient client) throws TelegramApiException {
        EditMessageReplyMarkup message = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(getData().getMessageId())
                .replyMarkup(null)
                .build();
        client.execute(message);
        RawBoardEntry entry = new RawBoardEntry(
                getData().getContractString(),
                getData().getDeclarer(),
                getData().getLeadString(),
                getData().getSign() != null ? getData().getResultInt() : 0
        );
        PairBoardResult result;
        if (!getData().isTournamentDeal()) {
            result = service.addBoardEntryImps(username, getData().getBoardNumber(), entry, 0d);
        }
        else {
            result = service.addTournamentBoardEntry(username, getData().getBoardNumber(), entry);
        }
        File file = protocolCreator.create(result.protocol(), "IMP");
        SendDocument document = SendDocument.builder()
                .chatId(chatId)
                .caption(String.format("Очки: %d. %s: %.1f", result.points(), result.countType(), result.duplicatePoints()))
                .document(new InputFile(file))
                .build();
        client.execute(document);
    }
}
