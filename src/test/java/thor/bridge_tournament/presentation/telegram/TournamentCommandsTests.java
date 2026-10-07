package thor.bridge_tournament.presentation.telegram;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.exception.TournamentNotFoundException;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.dto.tournament.TournamentPlayers;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.presentation.telegram.handler.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TournamentCommandsTests {
    private final UUID ownerId = UUID.randomUUID();
    private final UserDto owner = new UserDto(ownerId, "owner", "Судья", "Орлов", 1.0);
    private final UserDto alice = new UserDto(UUID.randomUUID(), "same_username", "Анна", "Иванова", 2.0);
    private final UserDto bob = new UserDto(UUID.randomUUID(), "same_username", "Борис", "Петров", 3.0);
    private final TournamentService tournaments = mock(TournamentService.class);
    private final UserService users = mock(UserService.class);
    private final TelegramClient client = mock(TelegramClient.class);

    @BeforeEach
    void defaults() {
        when(users.getAllPlayers()).thenReturn(List.of(owner, bob, alice));
        when(users.findByUsername(eq(IdentityProvider.TELEGRAM), anyString())).thenAnswer(invocation -> {
            String username = invocation.getArgument(1);
            return users.getAllPlayers().stream().filter(user -> user.username() != null && !username.isBlank()
                    && user.username().equalsIgnoreCase(username)).toList();
        });
        when(tournaments.getTds(ownerId)).thenReturn(List.of(owner));
        when(tournaments.getAllPlayers(ownerId)).thenReturn(new TournamentPlayers(List.of(), List.of()));
    }

    @Test
    void creationWizardCallsServiceWithResolvedOwnerAndAllParameters() throws Exception {
        var session = new TournamentCreationHandler(tournaments).handle(message("/create"), client, ownerId).orElseThrow();
        assertFalse(session.handleMessage(message("IMP"), client));
        assertFalse(session.handleMessage(message("8"), client));
        assertFalse(session.handleMessage(message("Клубный турнир"), client));
        verify(tournaments, never()).createTournament(any(), anyInt(), anyString(), anyString(), any());
        assertTrue(session.handleMessage(message("4"), client));
        verify(tournaments).createTournament(ownerId, 8, "IMP", "Клубный турнир", 4);
    }

    @Test
    void addsDirectorBySelectedUuidEvenWhenUsernamesCoincide() throws Exception {
        var session = new AddTournamentDirectorHandler(tournaments, users)
                .handle(message("/addtd"), client, ownerId).orElseThrow();
        assertTrue(buttons().stream().noneMatch(button -> button.getCallbackData().endsWith(ownerId.toString())));
        assertTrue(session.handleMessage(callback(userButton(bob)), client));
        verify(tournaments).addTournamentDirector(ownerId, bob.id());
        verify(tournaments, never()).addTournamentDirector(ownerId, alice.id());
        verify(client).execute(any(AnswerCallbackQuery.class));
        assertTrue(lastMessage().getText().contains("Борис Петров (@same_username)"));
    }

    @Test
    void playerWithoutUsernameRegistersThemselfInTheNamedDirectorsTournament() throws Exception {
        var newcomer = new UserDto(UUID.randomUUID(), null, "Вера", "Соколова", 4.0);
        when(users.getAllPlayers()).thenReturn(List.of(owner, alice, bob, newcomer));
        when(tournaments.getAllPlayers(ownerId)).thenReturn(new TournamentPlayers(
                List.of(owner), List.of(new PairDto(UUID.randomUUID(), alice, bob))));
        var session = new AddTournamentPlayerHandler(tournaments, users)
                .handle(message("/addplayer"), client, newcomer.id()).orElseThrow();
        verifyNoInteractions(tournaments);
        assertTrue(lastMessage().getText().contains("username создателя или судьи"));
        assertTrue(session.handleMessage(message("  @OwNeR  "), client));
        verify(tournaments).addPlayer(ownerId, newcomer.id());
        verify(tournaments, never()).getAllPlayers(newcomer.id());
    }

    @Test
    void pairIncludesInitiatorAndResolvesDirectorAndPartnerByUsername() throws Exception {
        var partner = new UserDto(bob.id(), "partner", bob.name(), bob.surname(), bob.sportCategory());
        when(users.getAllPlayers()).thenReturn(List.of(owner, alice, partner));
        when(tournaments.getAllPlayers(ownerId)).thenReturn(new TournamentPlayers(List.of(alice, partner), List.of()));
        var session = new AddTournamentPairHandler(tournaments, users)
                .handle(message("/addpair"), client, alice.id()).orElseThrow();
        verifyNoInteractions(tournaments);
        assertFalse(session.handleMessage(callback("old:user:" + bob.id()), client));
        assertFalse(session.handleMessage(message("owner"), client));
        assertTrue(lastMessage().getText().contains("Первым игроком пары будете вы"));
        assertFalse(session.handleMessage(message("@same_username"), client));
        assertTrue(sentMessages().stream().anyMatch(message -> message.getText().contains("самим собой")));
        verify(tournaments, never()).addPair(any(), any(), any());
        assertTrue(session.handleMessage(message("@PARTNER"), client));
        assertTrue(session.handleMessage(message("@PARTNER"), client));
        verify(tournaments, times(1)).addPair(ownerId, alice.id(), bob.id());
        verify(tournaments, never()).getAllPlayers(alice.id());
    }

    @Test
    void userSelectionSupportsPaginationAndIgnoresInvalidInput() throws Exception {
        var candidates = IntStream.range(0, 20)
                .mapToObj(i -> new UserDto(UUID.randomUUID(), null, "Игрок %02d".formatted(i), null, null)).toList();
        when(users.getAllPlayers()).thenReturn(candidates);
        var session = new AddTournamentDirectorHandler(tournaments, users)
                .handle(message("/addtd"), client, ownerId).orElseThrow();
        String next = buttonEnding(":page:1");
        assertFalse(session.handleMessage(message("не кнопка"), client));
        assertFalse(session.handleMessage(callback(next.replace("page:1", "page:9999")), client));
        assertFalse(session.handleMessage(callback(next), client));
        assertTrue(lastMessage().getText().contains("Страница 2 из 3"));
        assertTrue(buttons().stream().allMatch(button -> button.getCallbackData().getBytes(StandardCharsets.UTF_8).length <= 64));
        assertTrue(session.handleMessage(callback(userButton(candidates.get(12))), client));
        verify(tournaments).addTournamentDirector(ownerId, candidates.get(12).id());
    }

    @Test
    void selectionCanBeCancelledAndEmptyCandidatesDoNotStartDialog() throws Exception {
        var session = new AddTournamentDirectorHandler(tournaments, users)
                .handle(message("/addtd"), client, ownerId).orElseThrow();
        assertTrue(session.handleMessage(callback(buttonEnding(":cancel")), client));
        verify(tournaments, never()).addTournamentDirector(any(), any());
        when(users.getAllPlayers()).thenReturn(List.of(owner));
        assertTrue(new AddTournamentDirectorHandler(tournaments, users)
                .handle(message("/addtd"), client, ownerId).isEmpty());
        assertTrue(lastMessage().getText().contains("Нет пользователей"));
    }

    @Test
    void retryingTelegramReplyDoesNotRepeatSuccessfulMutation() throws Exception {
        var session = new AddTournamentPlayerHandler(tournaments, users)
                .handle(message("/addplayer"), client, alice.id()).orElseThrow();
        var selection = message("@owner");
        when(client.execute(any(SendMessage.class))).thenThrow(new TelegramApiException("Reply failed")).thenReturn(new Message());
        assertFalse(session.handleMessage(selection, client));
        assertTrue(session.handleMessage(selection, client));
        verify(tournaments, times(1)).addPlayer(ownerId, alice.id());
    }

    @Test
    void unknownOrAmbiguousDirectorUsernameDoesNotRegisterAnyone() throws Exception {
        var session = new AddTournamentPlayerHandler(tournaments, users)
                .handle(message("/addplayer"), client, alice.id()).orElseThrow();
        assertFalse(session.handleMessage(message("@missing"), client));
        assertFalse(session.handleMessage(message("@"), client));
        assertFalse(session.handleMessage(message("@same_username"), client));
        assertTrue(sentMessages().stream().anyMatch(message -> message.getText().contains("не найден")));
        assertTrue(sentMessages().stream().anyMatch(message -> message.getText().contains("нескольких пользователей")));
        verifyNoInteractions(tournaments);
        assertTrue(session.handleMessage(message("owner"), client));
        verify(tournaments).addPlayer(ownerId, alice.id());
    }

    @Test
    void directorWithoutCurrentTournamentCanBeCorrected() throws Exception {
        var withoutTournament = new UserDto(UUID.randomUUID(), "no_tournament", null, null, 1.0);
        when(users.getAllPlayers()).thenReturn(List.of(owner, alice, withoutTournament));
        when(tournaments.getAllPlayers(withoutTournament.id())).thenThrow(new TournamentNotFoundException(withoutTournament.id()));
        var session = new AddTournamentPlayerHandler(tournaments, users)
                .handle(message("/addplayer"), client, alice.id()).orElseThrow();
        assertFalse(session.handleMessage(message("no_tournament"), client));
        assertTrue(sentMessages().stream().anyMatch(message -> message.getText().contains("нет текущего турнира в роли судьи")));
        verify(tournaments, never()).addPlayer(any(), any());
        assertTrue(session.handleMessage(message("owner"), client));
        verify(tournaments).addPlayer(ownerId, alice.id());
    }

    @Test
    void usernameLookupUsesTelegramProviderAndIgnoresUnlinkedLegacyProfile() throws Exception {
        var legacy = new UserDto(UUID.randomUUID(), "owner", "Старый профиль", null, 1.0);
        when(users.getAllPlayers()).thenReturn(List.of(owner, legacy, alice));
        doReturn(List.of(owner)).when(users).findByUsername(IdentityProvider.TELEGRAM, "owner");
        var session = new AddTournamentPlayerHandler(tournaments, users)
                .handle(message("/addplayer"), client, alice.id()).orElseThrow();
        assertTrue(session.handleMessage(message("@owner"), client));
        verify(users).findByUsername(IdentityProvider.TELEGRAM, "owner");
        verify(tournaments).addPlayer(ownerId, alice.id());
        verify(tournaments, never()).getAllPlayers(legacy.id());
    }

    @Test
    void alreadyRegisteredInitiatorCannotBeAddedAgain() throws Exception {
        when(tournaments.getAllPlayers(ownerId)).thenReturn(new TournamentPlayers(List.of(alice), List.of()));
        var session = new AddTournamentPlayerHandler(tournaments, users)
                .handle(message("/addplayer"), client, alice.id()).orElseThrow();
        assertFalse(session.handleMessage(message("owner"), client));
        assertTrue(sentMessages().stream().anyMatch(message -> message.getText().contains("уже записаны")));
        when(tournaments.getAllPlayers(ownerId)).thenReturn(new TournamentPlayers(List.of(), List.of(new PairDto(UUID.randomUUID(), alice, bob))));
        assertFalse(session.handleMessage(message("owner"), client));
        var pairSession = new AddTournamentPairHandler(tournaments, users)
                .handle(message("/addpair"), client, alice.id()).orElseThrow();
        assertFalse(pairSession.handleMessage(message("owner"), client));
        verify(tournaments, never()).addPlayer(any(), any());
        verify(tournaments, never()).addPair(any(), any(), any());
    }

    @Test
    void unknownAmbiguousOrAlreadyPairedPartnerCanBeCorrected() throws Exception {
        var partner = new UserDto(UUID.randomUUID(), "partner", "Партнёр", null, 2.0);
        var occupied = new UserDto(UUID.randomUUID(), "occupied", "Другой игрок", null, 2.0);
        when(users.getAllPlayers()).thenReturn(List.of(owner, alice, bob, partner, occupied));
        when(tournaments.getAllPlayers(ownerId)).thenReturn(new TournamentPlayers(List.of(alice, partner),
                List.of(new PairDto(UUID.randomUUID(), occupied, bob))));
        var session = new AddTournamentPairHandler(tournaments, users)
                .handle(message("/addpair"), client, alice.id()).orElseThrow();
        assertFalse(session.handleMessage(message("owner"), client));
        for (String username : List.of("@unknown", "same_username", "occupied")) {
            assertFalse(session.handleMessage(message(username), client));
        }
        verify(tournaments, never()).addPair(any(), any(), any());
        assertTrue(session.handleMessage(message("partner"), client));
        verify(tournaments).addPair(ownerId, alice.id(), partner.id());
    }

    @Test
    void pairRechecksInitiatorParticipationAfterDirectorStep() throws Exception {
        var partner = new UserDto(bob.id(), "partner", bob.name(), bob.surname(), bob.sportCategory());
        when(users.getAllPlayers()).thenReturn(List.of(owner, alice, partner));
        var session = new AddTournamentPairHandler(tournaments, users)
                .handle(message("/addpair"), client, alice.id()).orElseThrow();
        assertFalse(session.handleMessage(message("owner"), client));
        when(tournaments.getAllPlayers(ownerId)).thenReturn(new TournamentPlayers(List.of(),
                List.of(new PairDto(UUID.randomUUID(), alice, owner))));
        assertFalse(session.handleMessage(message("partner"), client));
        verify(tournaments, never()).addPair(any(), any(), any());
    }

    @Test
    void startRequiresItsOwnConfirmationAndRunsOnlyOnce() throws Exception {
        when(tournaments.getAllPlayers(ownerId)).thenReturn(new TournamentPlayers(List.of(alice), List.of()));
        var session = new StartTournamentHandler(tournaments)
                .handle(message("/starttournament"), client, ownerId).orElseThrow();
        assertTrue(lastMessage().getText().contains("нечётное"));
        String confirm = buttonEnding(":start");
        assertFalse(session.handleMessage(callback("old:start"), client));
        verify(tournaments, never()).startTournament(any());
        assertTrue(session.handleMessage(callback(confirm), client));
        assertTrue(session.handleMessage(callback(confirm), client));
        verify(tournaments, times(1)).startTournament(ownerId);
    }

    @Test
    void cancellingStartDoesNotStartTournament() throws Exception {
        var session = new StartTournamentHandler(tournaments)
                .handle(message("/starttournament"), client, ownerId).orElseThrow();
        assertTrue(session.handleMessage(callback(buttonEnding(":cancel")), client));
        verify(tournaments, never()).startTournament(any());
    }

    @Test
    void boardDialogValidatesInputAndPassesDatabaseIdToService() throws Exception {
        var session = new AddTournamentBoardHandler(tournaments)
                .handle(message("/addtournamentboard"), client, ownerId).orElseThrow();
        for (String input : List.of("abc", "0", "-2", "999999999999999")) {
            assertFalse(session.handleMessage(message(input), client));
        }
        assertFalse(session.handleMessage(callback("old:button"), client));
        verify(tournaments, never()).addBoardToTournament(anyInt(), any());
        assertTrue(session.handleMessage(message(" 12345 "), client));
        verify(tournaments).addBoardToTournament(12345, ownerId);
    }

    @Test
    void directorAndPlayerListsContainProfilesAndSeparatePairsFromUnpairedPlayers() throws Exception {
        var noName = new UserDto(UUID.randomUUID(), null, null, null, null);
        when(tournaments.getTds(ownerId)).thenReturn(List.of(alice, noName));
        assertTrue(new GetTournamentDirectorsHandler(tournaments).handle(message("/tds"), client, ownerId).isEmpty());
        assertTrue(lastMessage().getText().contains("Анна Иванова (@same_username)"));
        assertTrue(lastMessage().getText().contains("Пользователь без имени"));
        assertFalse(lastMessage().getText().contains("null"));
        when(tournaments.getAllPlayers(ownerId)).thenReturn(new TournamentPlayers(
                List.of(owner), List.of(new PairDto(UUID.randomUUID(), alice, bob))));
        assertTrue(new GetTournamentPlayersHandler(tournaments).handle(message("/players"), client, ownerId).isEmpty());
        assertTrue(lastMessage().getText().contains("Участники турнира: 3"));
        assertTrue(lastMessage().getText().contains("Пары: 1"));
        assertTrue(lastMessage().getText().contains("Без пары: 1"));
        assertNull(lastMessage().getParseMode());
    }

    @Test
    void longListsAreSplitAndUserProvidedTextIsNotInterpretedAsMarkup() throws Exception {
        var profiles = IntStream.range(0, 100)
                .mapToObj(i -> new UserDto(UUID.randomUUID(), "player" + i, "<b>Имя</b>" + "😀".repeat(100), "Фамилия".repeat(100), null))
                .toList();
        TournamentMessages.directors(client, 1L, profiles);
        var messages = sentMessages();
        assertTrue(messages.size() > 1);
        assertTrue(messages.stream().allMatch(message -> message.getText().length() <= 4096 && message.getParseMode() == null));
        assertTrue(messages.getFirst().getText().contains("<b>Имя</b>"));
        for (int i = 0; i < 100; i++) {
            String handle = "(@player" + i + ")";
            assertEquals(1, messages.stream().filter(message -> message.getText().contains(handle)).count());
        }
    }

    @Test
    void missingCurrentTournamentIsReportedBeforeStartingUserSelection() {
        when(tournaments.getTds(ownerId)).thenThrow(new TournamentNotFoundException(ownerId));
        assertThrows(TournamentNotFoundException.class, () -> new AddTournamentDirectorHandler(tournaments, users)
                .handle(message("/addtd"), client, ownerId));
        verifyNoInteractions(users, client);
    }

    private List<SendMessage> sentMessages() {
        return mockingDetails(client).getInvocations().stream().map(invocation -> invocation.getArgument(0))
                .filter(SendMessage.class::isInstance).map(SendMessage.class::cast).toList();
    }

    private SendMessage lastMessage() {
        return sentMessages().getLast();
    }

    private List<InlineKeyboardButton> buttons() {
        var keyboard = (InlineKeyboardMarkup) lastMessage().getReplyMarkup();
        return keyboard.getKeyboard().stream().flatMap(List::stream).toList();
    }

    private String userButton(UserDto user) {
        return buttonEnding(":user:" + user.id());
    }

    private String buttonEnding(String suffix) {
        return buttons().stream().map(InlineKeyboardButton::getCallbackData)
                .filter(data -> data.endsWith(suffix)).findFirst().orElseThrow();
    }

    static Update message(String text) {
        var message = new Message();
        message.setChat(new Chat(100L, "private"));
        message.setFrom(new User(5_000_000_001L, "Director", false));
        message.setText(text);
        var update = new Update();
        update.setMessage(message);
        return update;
    }

    static Update callback(String data) {
        var callback = new CallbackQuery();
        callback.setId(UUID.randomUUID().toString());
        callback.setFrom(new User(5_000_000_001L, "Director", false));
        callback.setMessage(message("buttons").getMessage());
        callback.setData(data);
        var update = new Update();
        update.setCallbackQuery(callback);
        return update;
    }
}
