package thor.bridge_tournament.presentation.telegram.session;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.presentation.html.HtmlProtocolCreator;
import thor.bridge_tournament.presentation.telegram.TelegramUtils;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;
import thor.bridge_tournament.presentation.telegram.session.state.entry.data.AddEntryData;

import java.util.Locale;
import java.util.UUID;

public abstract class AbstractAddEntrySession extends AbstractSessionWithState<AddEntryData> {
    private final HtmlProtocolCreator protocolCreator;
    protected final UUID userId;
    private final long telegramUserId;
    private final long chatId;
    private PairBoardResult savedResult;
    private boolean documentSent;

    protected AbstractAddEntrySession(Update update, TelegramClient client, HtmlProtocolCreator protocolCreator,
                                      UUID userId, SessionState<AddEntryData> state, AddEntryData data) {
        super(state, update, client, data);
        this.protocolCreator = protocolCreator;
        this.userId = userId;
        this.telegramUserId = TelegramUtils.getUser(update).getId();
        this.chatId = TelegramUtils.getChatId(update);
    }

    protected abstract PairBoardResult saveEntry(RawBoardEntry entry);

    @Override
    public boolean handleMessage(Update update, TelegramClient client) {
        if (TelegramUtils.getUser(update).getId() != telegramUserId || TelegramUtils.getChatId(update) != chatId) {
            return false;
        }
        if (savedResult != null) {
            try {
                if (update.hasCallbackQuery()) {
                    client.execute(AnswerCallbackQuery.builder().callbackQueryId(update.getCallbackQuery().getId()).build());
                }
                finishInteractiveChain(TelegramUtils.getChatId(update), client);
                return true;
            } catch (TelegramApiException e) {
                return false;
            }
        }
        return super.handleMessage(update, client);
    }

    @Override
    protected void finishInteractiveChain(long chatId, TelegramClient client) throws TelegramApiException {
        if (savedResult == null) {
            var data = getData();
            savedResult = saveEntry(new RawBoardEntry(data.getContractString(), data.getDeclarer(),
                    data.getLeadString(), data.getSign() != null ? data.getResultInt() : 0));
        }
        if (!documentSent) {
            var file = protocolCreator.create(savedResult.protocol(), savedResult.countType());
            try {
                client.execute(SendDocument.builder().chatId(chatId)
                        .caption(String.format(Locale.ROOT, "%s: %d. Очки: %d. %s: %.2f",
                                getData().getBoardLabel(), getData().getBoardNumber(), savedResult.points(),
                                savedResult.countType(), savedResult.duplicatePoints()))
                        .document(new InputFile(file)).build());
                documentSent = true;
            } finally {
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
        }
        client.execute(EditMessageReplyMarkup.builder().chatId(chatId)
                .messageId(getData().getMessageId()).replyMarkup(null).build());
    }
}
