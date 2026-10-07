package thor.bridge_tournament.presentation.telegram;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.objects.User;
import thor.bridge_tournament.core.port.input.UserIdentityService;
import thor.bridge_tournament.presentation.telegram.exception.ExceptionHandler;
import thor.bridge_tournament.presentation.telegram.handler.CancelCommandHandler;
import thor.bridge_tournament.presentation.telegram.handler.CommandHandler;
import thor.bridge_tournament.presentation.telegram.handler.MainCommandHandler;
import thor.bridge_tournament.presentation.telegram.session.UserSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static thor.bridge_tournament.presentation.telegram.TournamentCommandsTests.message;

class TournamentSessionRoutingTests {
    @Test
    void cancelRemovesTheSessionBeforeFurtherInputCanModifyTournament() throws Exception {
        var session = mock(UserSession.class);
        var command = selectionCommand(session);
        var identity = mock(UserIdentityService.class);
        when(identity.resolveOrRegister(any(), isNull())).thenReturn(UUID.randomUUID());
        var dispatcher = new MainCommandHandler(List.of(command, new CancelCommandHandler()), identity);
        try (var clients = mockConstruction(OkHttpTelegramClient.class)) {
            var bot = new TelegramBotMainClass("test-token", dispatcher, new ExceptionHandler());
            bot.consume(message("/choose"));
            bot.consume(message("/cancel"));
            bot.consume(message("123"));

            verify(session, never()).handleMessage(any(), any());
        }
    }

    @Test
    void anotherUsersCancelAndCommandsForAnotherBotDoNotCancelOwnersDialog() throws Exception {
        var session = mock(UserSession.class);
        var command = selectionCommand(session);
        var identity = mock(UserIdentityService.class);
        when(identity.resolveOrRegister(any(), isNull())).thenReturn(UUID.randomUUID());
        var dispatcher = new MainCommandHandler(List.of(command, new CancelCommandHandler()), identity);
        try (var clients = mockConstruction(OkHttpTelegramClient.class)) {
            var bot = new TelegramBotMainClass("test-token", dispatcher, new ExceptionHandler());
            bot.consume(message("/choose"));
            bot.consume(message("/cancel@AnotherBot"));
            var someoneElse = message("/cancel");
            someoneElse.getMessage().setFrom(new User(5_000_000_002L, "Other user", false));
            bot.consume(someoneElse);
            var answer = message("123");
            bot.consume(answer);

            verify(session).handleMessage(eq(answer), eq(clients.constructed().getFirst()));
        }
    }

    private CommandHandler selectionCommand(UserSession session) throws Exception {
        var command = mock(CommandHandler.class);
        when(command.getName()).thenReturn("choose");
        when(command.getDescription()).thenReturn("Выбрать игрока");
        when(command.canHandle("choose")).thenReturn(true);
        when(command.handle(any(), any(), any())).thenReturn(Optional.of(session));
        return command;
    }
}
