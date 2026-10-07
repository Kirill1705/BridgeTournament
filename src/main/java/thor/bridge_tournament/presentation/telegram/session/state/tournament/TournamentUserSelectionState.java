package thor.bridge_tournament.presentation.telegram.session.state.tournament;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.presentation.telegram.TournamentMessages;
import thor.bridge_tournament.presentation.telegram.session.SessionWithState;
import thor.bridge_tournament.presentation.telegram.session.state.SessionState;

import java.util.ArrayList;
import java.util.UUID;

public class TournamentUserSelectionState implements SessionState<TournamentUserSelectionData> {
    private static final int PAGE_SIZE = 8;
    private final String token = UUID.randomUUID().toString().substring(0, 8) + ":";
    private int page;

    @Override
    public void sendInfo(TelegramClient client, long chatId, TournamentUserSelectionData data) throws TelegramApiException {
        var users = data.remaining();
        int pages = (users.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        var rows = new ArrayList<InlineKeyboardRow>();
        for (int i = page * PAGE_SIZE; i < Math.min((page + 1) * PAGE_SIZE, users.size()); i++) {
            var user = users.get(i);
            rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text((i + 1) + ". " + TournamentMessages.shorten(TournamentMessages.user(user), 60))
                    .callbackData(token + "user:" + user.id()).build()));
        }
        var navigation = new InlineKeyboardRow();
        if (page > 0) {
            navigation.add(InlineKeyboardButton.builder().text("← Назад").callbackData(token + "page:" + (page - 1)).build());
        }
        if (page + 1 < pages) {
            navigation.add(InlineKeyboardButton.builder().text("Далее →").callbackData(token + "page:" + (page + 1)).build());
        }
        if (!navigation.isEmpty()) {
            rows.add(navigation);
        }
        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder().text("Отмена").callbackData(token + "cancel").build()));
        String prompt = switch (data.getAction()) {
            case DIRECTOR -> "Выберите нового судью турнира.";
            case PLAYER -> "Выберите игрока, которого нужно добавить без пары.";
            case PAIR -> data.getSelected().isEmpty() ? "Выберите первого игрока пары."
                    : "Первый игрок: " + TournamentMessages.user(data.getSelected().getFirst()) + "\nВыберите второго игрока.";
            case DIRECTOR_PAIR, REMOVE_PLAYER -> throw new IllegalStateException("This action uses username input");
        };
        client.execute(SendMessage.builder().chatId(chatId)
                .text(prompt + "\nСтраница " + (page + 1) + " из " + pages
                        + "\nНет нужного пользователя? Попросите его заполнить профиль: /register.")
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rows).build()).build());
    }

    @Override
    public boolean handle(Update update, TelegramClient client, SessionWithState<TournamentUserSelectionData> session) throws TelegramApiException {
        var data = session.getData();
        if (update.hasCallbackQuery()) {
            client.execute(AnswerCallbackQuery.builder().callbackQueryId(update.getCallbackQuery().getId()).build());
        }
        if (data.isComplete()) {
            return true;
        }
        if (!update.hasCallbackQuery() || update.getCallbackQuery().getData() == null
                || !update.getCallbackQuery().getData().startsWith(token)) {
            return false;
        }
        String action = update.getCallbackQuery().getData().substring(token.length());
        if (action.equals("cancel")) {
            data.setCancelled(true);
            return true;
        }
        try {
            if (action.startsWith("page:")) {
                int requestedPage = Integer.parseInt(action.substring(5));
                if (requestedPage >= 0 && requestedPage <= (data.remaining().size() - 1) / PAGE_SIZE) {
                    page = requestedPage;
                }
            } else if (action.startsWith("user:")) {
                UUID selectedId = UUID.fromString(action.substring(5));
                var selected = data.remaining().stream().filter(user -> user.id().equals(selectedId)).findFirst();
                if (selected.isPresent()) {
                    data.getSelected().add(selected.get());
                    if (data.isComplete()) {
                        return true;
                    }
                    // A new token prevents an old first-player button from selecting the partner.
                    session.updateState(new TournamentUserSelectionState());
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Malformed or outdated button data does not change the selection.
        }
        return false;
    }
}
