package thor.bridge_tournament.presentation.telegram;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;
import thor.bridge_tournament.core.exception.TournamentNotFoundException;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.dto.tournament.TournamentPlayers;
import thor.bridge_tournament.core.port.input.TournamentBoardEntryService;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.core.port.input.UserIdentityService;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.presentation.html.HtmlProtocolCreator;
import thor.bridge_tournament.presentation.telegram.handler.AddTournamentEntryByTdHandler;
import thor.bridge_tournament.presentation.telegram.handler.MainCommandHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static thor.bridge_tournament.presentation.telegram.TournamentCommandsTests.callback;
import static thor.bridge_tournament.presentation.telegram.TournamentCommandsTests.message;

class TournamentDirectorEntryCommandsTests {
    private final UserDto td = user("director");
    private final UserDto first = user("first");
    private final UserDto partner = user("partner");
    private final UserDto second = user("second");
    private final TournamentService tournaments = mock(TournamentService.class);
    private final TournamentBoardEntryService entries = mock(TournamentBoardEntryService.class);
    private final UserService users = mock(UserService.class);
    private final UserIdentityService identities = mock(UserIdentityService.class);
    private final HtmlProtocolCreator protocols = mock(HtmlProtocolCreator.class);
    private final TelegramClient client = mock(TelegramClient.class);
    @TempDir Path tempDir;

    @BeforeEach
    void defaults() throws Exception {
        when(tournaments.getTds(td.id())).thenReturn(List.of(td));
        when(tournaments.getAllPlayers(td.id())).thenReturn(new TournamentPlayers(List.of(), List.of(
                new PairDto(UUID.randomUUID(), first, partner), new PairDto(UUID.randomUUID(), second, user("other")))));
        for (var user : List.of(first, partner, second)) {
            when(users.findByUsername(IdentityProvider.TELEGRAM, user.username())).thenReturn(List.of(user));
        }
        when(identities.resolveOrRegister(any(), isNull())).thenReturn(td.id());
        when(client.execute(any(SendMessage.class))).thenAnswer(invocation -> {
            var sent = message("input").getMessage();
            sent.setMessageId(123);
            return sent;
        });
        when(entries.addTournamentBoardEntryByTd(eq(td.id()), eq(1), any(), eq(first.id()), eq(second.id())))
                .thenReturn(new PairBoardResult(UUID.randomUUID(), "MP", 420, 50, Map.of()));
        when(protocols.create(anyMap(), anyString())).thenAnswer(invocation ->
                Files.createTempFile(tempDir, "protocol-", ".html").toFile());
    }

    @Test
    void commandResolvesDirectorAndBothRepresentativesThenUsesSharedEntryWizard() throws Exception {
        var dispatcher = new MainCommandHandler(List.of(handler()), identities);
        var session = dispatcher.handle(message("/addtournamententrybytd"), client, "addtournamententrybytd").orElseThrow();
        assertFalse(session.handleMessage(message("@first"), client));
        assertFalse(session.handleMessage(message("second"), client));
        assertFalse(session.handleMessage(message("1"), client));
        verifyNoInteractions(entries);
        for (String input : List.of("4", "SPADES", "N", "CLUBS", "K", "=")) {
            assertFalse(session.handleMessage(button(input), client));
        }
        verifyNoInteractions(entries);
        assertTrue(session.handleMessage(button("ok"), client));
        verify(entries).addTournamentBoardEntryByTd(td.id(), 1, new RawBoardEntry("4S", "N", "CK", 0), first.id(), second.id());
        verify(entries, never()).addTournamentBoardEntryForPlayer(any(), anyInt(), any());
        verify(protocols).create(Map.of(), "MP");
        verify(client).execute(any(SendDocument.class));
    }

    @Test
    void unknownUnpairedAndSamePairPlayersAreRejectedAndCanBeCorrected() throws Exception {
        var unpaired = user("unpaired");
        when(users.findByUsername(IdentityProvider.TELEGRAM, "unpaired")).thenReturn(List.of(unpaired));
        var session = handler().handle(message("/addtournamententrybytd"), client, td.id()).orElseThrow();
        assertFalse(session.handleMessage(message("missing"), client));
        assertFalse(session.handleMessage(message("unpaired"), client));
        assertFalse(session.handleMessage(message("first"), client));
        assertFalse(session.handleMessage(message("partner"), client));
        verifyNoInteractions(entries);
        assertFalse(session.handleMessage(message("second"), client));
        assertFalse(session.handleMessage(message("1"), client));
        assertFalse(session.handleMessage(button("pass"), client));
        assertTrue(session.handleMessage(button("ok"), client));
        verify(entries).addTournamentBoardEntryByTd(td.id(), 1, new RawBoardEntry("pass", null, null, 0), first.id(), second.id());
    }

    @Test
    void onlyDirectorWhoStartedTheSessionCanConfirmTheResult() throws Exception {
        var session = handler().handle(message("/addtournamententrybytd"), client, td.id()).orElseThrow();
        for (String input : List.of("first", "second", "1")) {
            assertFalse(session.handleMessage(message(input), client));
        }
        assertFalse(session.handleMessage(button("pass"), client));
        var foreign = button("ok");
        foreign.getCallbackQuery().setFrom(new User(5_000_000_002L, "Other director", false));
        assertFalse(session.handleMessage(foreign, client));
        verifyNoInteractions(entries);
        assertTrue(session.handleMessage(button("ok"), client));
    }

    @Test
    void playerWithoutDirectorTournamentCannotStartTheCommand() {
        when(tournaments.getTds(first.id())).thenThrow(new TournamentNotFoundException(first.id()));
        assertThrows(TournamentNotFoundException.class, () -> handler().handle(message("/addtournamententrybytd"), client, first.id()));
        verifyNoInteractions(users, entries, client);
    }

    private AddTournamentEntryByTdHandler handler() {
        return new AddTournamentEntryByTdHandler(entries, tournaments, users, protocols);
    }

    private Update button(String input) {
        var update = callback(input);
        ((Message) update.getCallbackQuery().getMessage()).setMessageId(123);
        return update;
    }

    private UserDto user(String username) { return new UserDto(UUID.randomUUID(), username, null, null, 5d); }
}
