package thor.bridge_tournament.presentation.telegram;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.domain.identity.ExternalIdentity;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.dto.movement.PairMovementEntryDto;
import thor.bridge_tournament.core.port.dto.movement.PairMovementNextRoundInfo;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.dto.tournament_result.PairTournamentResultDto;
import thor.bridge_tournament.core.port.input.MovementService;
import thor.bridge_tournament.core.port.input.BoardEntryService;
import thor.bridge_tournament.core.port.input.TournamentBoardEntryService;
import thor.bridge_tournament.core.port.input.TournamentResultService;
import thor.bridge_tournament.core.port.input.UserIdentityService;
import thor.bridge_tournament.presentation.html.HtmlProtocolCreator;
import thor.bridge_tournament.presentation.telegram.exception.ExceptionHandler;
import thor.bridge_tournament.presentation.telegram.handler.AddTournamentEntryHandler;
import thor.bridge_tournament.presentation.telegram.handler.AddEntryHandler;
import thor.bridge_tournament.presentation.telegram.handler.GetTournamentResultsHandler;
import thor.bridge_tournament.presentation.telegram.handler.MainCommandHandler;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static thor.bridge_tournament.presentation.telegram.TournamentCommandsTests.callback;
import static thor.bridge_tournament.presentation.telegram.TournamentCommandsTests.message;

class TournamentBoardEntryCommandsTests {
    private static final int INPUT_MESSAGE_ID = 123;
    private final UUID playerId = UUID.randomUUID();
    private final TournamentBoardEntryService entries = mock(TournamentBoardEntryService.class);
    private final MovementService movements = mock(MovementService.class);
    private final HtmlProtocolCreator protocols = mock(HtmlProtocolCreator.class);
    private final UserIdentityService identities = mock(UserIdentityService.class);
    private final TelegramClient client = mock(TelegramClient.class);
    @TempDir
    Path tempDir;

    @BeforeEach
    void defaults() throws Exception {
        stubMessages(client);
        when(identities.resolveOrRegister(new ExternalIdentity(IdentityProvider.TELEGRAM, "5000000001"), null))
                .thenReturn(playerId);
        when(movements.getMovementNextRound(playerId)).thenReturn(new PairMovementNextRoundInfo(
                new PairMovementEntryDto(2, null, 3, List.of(7, 8)), List.of(7, 8)));
        when(protocols.create(anyMap(), anyString())).thenAnswer(invocation ->
                Files.createTempFile(tempDir, "protocol-", ".html").toFile());
        when(entries.addTournamentBoardEntry(eq(playerId), eq(7), any()))
                .thenReturn(new PairBoardResult(UUID.randomUUID(), "IMP", 450, 0, Map.of()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"IMP", "MP"})
    void fullWizardKeepsCommandAuthorsUuidAndSavesOnlyAfterConfirmation(String countType) throws Exception {
        when(entries.addTournamentBoardEntry(eq(playerId), eq(7), any()))
                .thenReturn(new PairBoardResult(UUID.randomUUID(), countType, 450, 50, Map.of()));
        var dispatcher = dispatcher();
        var session = dispatcher.handle(message("/addtournamententry"), client, "addtournamententry").orElseThrow();

        assertFalse(session.handleMessage(message("7"), client));
        assertTrue(lastEditorMessage().text().contains("Выберите кнопками уровень и масть контракта"));
        assertTrue(buttons().containsAll(List.of("SPADES", "4", "N")));
        verifyNoInteractions(entries);
        for (String input : List.of("4", "SPADES", "N", "CLUBS", "K", "+", "1")) {
            assertFalse(session.handleMessage(button(input), client));
            verifyNoInteractions(entries);
        }
        assertTrue(buttons().contains("ok"));
        assertTrue(session.handleMessage(button("ok"), client));

        verify(identities).resolveOrRegister(new ExternalIdentity(IdentityProvider.TELEGRAM, "5000000001"), null);
        verify(entries).addTournamentBoardEntry(playerId, 7, new RawBoardEntry("4S", "N", "CK", 1));
        verify(protocols).create(Map.of(), countType);
        verify(client).execute(any(SendDocument.class));
    }

    @Test
    void invalidOrOtherRoundsBoardCannotSaveAndCanBeCorrected() throws Exception {
        var session = startSession();
        for (String input : List.of("abc", "0", "-1", "99999999999999999", "99")) {
            assertFalse(session.handleMessage(message(input), client));
        }
        assertFalse(session.handleMessage(message("7"), client));
        assertTrue(buttons().contains("SPADES"));
        verifyNoInteractions(entries);
    }

    @Test
    void boardSelectionSendsContractPromptAndButtonsTogetherWithoutEditingAnEmptyForm() throws Exception {
        var session = startSession();
        assertFalse(session.handleMessage(message("7"), client));
        var prompt = lastEditorMessage();
        assertTrue(prompt.text().contains("Номер сдачи: 7"));
        assertTrue(prompt.text().contains("Выберите кнопками уровень и масть контракта"));
        assertTrue(buttons().containsAll(List.of("4", "SPADES", "N")));
        verify(client, never()).execute(any(EditMessageText.class));
        verifyNoInteractions(entries);
    }

    @ParameterizedTest
    @ValueSource(strings = {"IMP", "MP"})
    void standaloneEntryAlsoShowsButtonsImmediatelyAndReusesTheWholeWizard(String countType) throws Exception {
        var standalone = mock(BoardEntryService.class);
        var result = new PairBoardResult(UUID.randomUUID(), countType, 420, 50, Map.of());
        when(standalone.addBoardEntryImps(eq(playerId), eq(107), any(), eq(0d))).thenReturn(result);
        when(standalone.addBoardEntryMp(eq(playerId), eq(107), any())).thenReturn(result);
        var dispatcher = new MainCommandHandler(List.of(new AddEntryHandler(standalone, protocols)), identities);
        var session = dispatcher.handle(message("/addentry"), client, "addentry").orElseThrow();
        assertFalse(session.handleMessage(message(countType), client));
        assertFalse(session.handleMessage(message("107"), client));
        verify(client, never()).execute(any(EditMessageText.class));
        assertTrue(buttons().contains("SPADES"));
        verifyNoInteractions(standalone);
        for (String input : List.of("4", "SPADES", "N", "CLUBS", "K", "=")) {
            assertFalse(session.handleMessage(button(input), client));
        }
        verifyNoInteractions(standalone);
        assertTrue(session.handleMessage(button("ok"), client));
        var raw = new RawBoardEntry("4S", "N", "CK", 0);
        if (countType.equals("IMP")) {
            verify(standalone).addBoardEntryImps(playerId, 107, raw, 0d);
            verify(standalone, never()).addBoardEntryMp(any(), anyInt(), any());
        } else {
            verify(standalone).addBoardEntryMp(playerId, 107, raw);
            verify(standalone, never()).addBoardEntryImps(any(), anyInt(), any(), anyDouble());
        }
    }

    @Test
    void failureToSendContractKeyboardKeepsSessionAndCanBeRetriedWithoutSaving() throws Exception {
        var session = startSession();
        doThrow(new TelegramApiException("Send failed")).doAnswer(invocation -> {
            var sent = message("input").getMessage();
            sent.setMessageId(INPUT_MESSAGE_ID);
            return sent;
        }).when(client).execute(argThat((BotApiMethod<?> request) -> request instanceof SendMessage sent
                && sent.getReplyMarkup() instanceof InlineKeyboardMarkup));
        assertFalse(session.handleMessage(message("7"), client));
        verifyNoInteractions(entries);
        assertFalse(session.handleMessage(message("повтор"), client));
        assertTrue(buttons().contains("SPADES"));
        assertFalse(session.handleMessage(button("pass"), client));
        assertTrue(session.handleMessage(button("ok"), client));
        verify(entries).addTournamentBoardEntry(playerId, 7, new RawBoardEntry("pass", null, null, 0));
    }

    @Test
    void passAlsoRequiresConfirmationAndBackReturnsToContractSelection() throws Exception {
        var session = startSession();
        assertFalse(session.handleMessage(message("7"), client));
        assertFalse(session.handleMessage(button("pass"), client));
        assertFalse(session.handleMessage(button("back"), client));
        assertTrue(buttons().contains("SPADES"));
        assertFalse(session.handleMessage(button("pass"), client));
        verifyNoInteractions(entries);
        assertTrue(session.handleMessage(button("ok"), client));
        verify(entries).addTournamentBoardEntry(playerId, 7, new RawBoardEntry("pass", null, null, 0));
    }

    @Test
    void staleConfirmationAndForeignUsersInputCannotAdvanceOwnersSession() throws Exception {
        var session = startSession();
        assertFalse(session.handleMessage(message("7"), client));
        for (String input : List.of("4", "SPADES", "N", "CLUBS", "K", "=")) {
            assertFalse(session.handleMessage(button(input), client));
        }
        var stale = button("ok");
        ((Message) stale.getCallbackQuery().getMessage()).setMessageId(INPUT_MESSAGE_ID - 1);
        assertFalse(session.handleMessage(stale, client));
        var foreign = button("ok");
        foreign.getCallbackQuery().setFrom(new User(5_000_000_002L, "Another player", false));
        assertFalse(session.handleMessage(foreign, client));
        verifyNoInteractions(entries);
        assertTrue(session.handleMessage(button("ok"), client));
        verify(entries).addTournamentBoardEntry(playerId, 7, new RawBoardEntry("4S", "N", "CK", 0));
    }

    @Test
    void foreignBoardNumbersAndButtonsFromAnotherChatCannotModifyDialog() throws Exception {
        var session = startSession();
        var foreign = message("7");
        foreign.getMessage().setFrom(new User(5_000_000_002L, "Another player", false));
        assertFalse(session.handleMessage(foreign, client));
        verify(client, never()).execute(argThat((BotApiMethod<?> request) -> request instanceof SendMessage sent
                && sent.getReplyMarkup() instanceof InlineKeyboardMarkup));
        verify(client, never()).execute(any(EditMessageText.class));
        assertFalse(session.handleMessage(message("7"), client));
        var otherChat = button("pass");
        ((Message) otherChat.getCallbackQuery().getMessage()).getChat().setId(101L);
        assertFalse(session.handleMessage(otherChat, client));
        assertTrue(buttons().contains("SPADES"));
        verifyNoInteractions(entries);
    }

    @Test
    void telegramDeliveryRetryDoesNotSaveTheResultAgain() throws Exception {
        var session = startSession();
        assertFalse(session.handleMessage(message("7"), client));
        assertFalse(session.handleMessage(button("pass"), client));
        when(client.execute(any(SendDocument.class))).thenThrow(new TelegramApiException("Upload failed"))
                .thenReturn(message("protocol").getMessage());
        assertFalse(session.handleMessage(button("ok"), client));
        assertTrue(session.handleMessage(button("ok"), client));
        verify(entries, times(1)).addTournamentBoardEntry(playerId, 7, new RawBoardEntry("pass", null, null, 0));
    }

    @Test
    void botKeepsWizardAfterBoardNumberAndRoutesSharedChatInputByTelegramSender() throws Exception {
        try (var clients = mockConstruction(OkHttpTelegramClient.class, (mock, context) -> stubMessages(mock))) {
            var bot = new TelegramBotMainClass("test-token", dispatcher(), new ExceptionHandler());
            bot.consume(message("/addtournamententry"));
            bot.consume(message("7"));
            var foreign = button("pass");
            foreign.getCallbackQuery().setFrom(new User(5_000_000_002L, "Another player", false));
            bot.consume(foreign);
            verifyNoInteractions(entries);
            bot.consume(button("pass"));
            bot.consume(button("ok"));
            verify(entries).addTournamentBoardEntry(playerId, 7, new RawBoardEntry("pass", null, null, 0));
            verify(clients.constructed().getFirst()).execute(any(SendDocument.class));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"IMP", "MP"})
    void resultsCommandUsesCommandAuthorAndDisplaysBothPlayersAndScore(String countType) throws Exception {
        var results = mock(TournamentResultService.class);
        var pair = new PairDto(UUID.randomUUID(), new UserDto(UUID.randomUUID(), "first", null, null, 5d),
                new UserDto(UUID.randomUUID(), "second", null, null, 5d));
        when(results.getRanks(playerId)).thenReturn(List.of(new PairTournamentResultDto(1, pair, 50d, countType)));
        var dispatcher = new MainCommandHandler(List.of(new GetTournamentResultsHandler(results)), identities);
        assertTrue(dispatcher.handle(message("/tournamentresults"), client, "tournamentresults").isEmpty());
        verify(results).getRanks(playerId);
        var text = mockingDetails(client).getInvocations().stream().map(invocation -> invocation.getArgument(0))
                .filter(SendMessage.class::isInstance).map(SendMessage.class::cast).map(SendMessage::getText)
                .reduce("", (first, second) -> first + second);
        assertTrue(text.contains("1. @first — @second"));
        assertTrue(text.contains(countType.equals("MP") ? "50.00 %" : "50.00 IMP"));
    }

    private MainCommandHandler dispatcher() {
        return new MainCommandHandler(List.of(new AddTournamentEntryHandler(entries, movements, protocols)), identities);
    }

    private UserSession startSession() throws Exception {
        return dispatcher().handle(message("/addtournamententry"), client, "addtournamententry").orElseThrow();
    }

    private void stubMessages(TelegramClient client) throws Exception {
        when(client.execute(any(SendMessage.class))).thenAnswer(invocation -> {
            var sent = message("input").getMessage();
            sent.setMessageId(INPUT_MESSAGE_ID);
            sent.setFrom(new User(999L, "Bot", true));
            return sent;
        });
    }

    private Update button(String data) {
        var update = callback(data);
        ((Message) update.getCallbackQuery().getMessage()).setMessageId(INPUT_MESSAGE_ID);
        ((Message) update.getCallbackQuery().getMessage()).setFrom(new User(999L, "Bot", true));
        return update;
    }

    private List<String> buttons() {
        var last = lastEditorMessage();
        return last.keyboard().getKeyboard().stream().flatMap(List::stream)
                .map(button -> button.getCallbackData()).toList();
    }

    private InputPrompt lastEditorMessage() {
        return mockingDetails(client).getInvocations().stream().map(invocation -> invocation.getArgument(0))
                .filter(request -> request instanceof EditMessageText
                        || request instanceof SendMessage sent && sent.getReplyMarkup() instanceof InlineKeyboardMarkup)
                .map(request -> request instanceof EditMessageText edited
                        ? new InputPrompt(edited.getText(), edited.getReplyMarkup())
                        : new InputPrompt(((SendMessage) request).getText(), (InlineKeyboardMarkup) ((SendMessage) request).getReplyMarkup()))
                .toList().getLast();
    }

    private record InputPrompt(String text, InlineKeyboardMarkup keyboard) {}
}
