package thor.bridge_tournament.presentation.telegram;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.domain.identity.ExternalIdentity;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;
import thor.bridge_tournament.core.port.input.UserIdentityService;
import thor.bridge_tournament.presentation.telegram.handler.CommandHandler;
import thor.bridge_tournament.presentation.telegram.handler.MainCommandHandler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelegramIdentityTests {
    @Test
    void groupCommandSuffixIsNormalizedAndCommandsForOtherBotsAreIgnored() throws Exception {
        var identities = mock(UserIdentityService.class);
        var command = mock(CommandHandler.class);
        var client = mock(TelegramClient.class);
        var userId = UUID.randomUUID();
        var update = TournamentCommandsTests.message("/ADDTD@BridgeTournamentBot");
        when(identities.resolveOrRegister(any(), isNull())).thenReturn(userId);
        when(command.canHandle("addtd")).thenReturn(true);
        when(command.handle(update, client, userId)).thenReturn(Optional.empty());
        var dispatcher = new MainCommandHandler(List.of(command), identities);

        assertFalse(dispatcher.isAddressedToThisBot("addtd@OtherBot"));
        assertTrue(dispatcher.handle(update, client, "addtd@OtherBot").isEmpty());
        verifyNoInteractions(identities, command, client);
        dispatcher.handle(update, client, "ADDTD@BridgeTournamentBot");
        verify(command).handle(update, client, userId);
    }

    @Test
    void commandResolvesNumericTelegramIdAndPassesInternalUuidToHandler() throws Exception {
        var sender = new User(5_000_000_001L, "Player", false);
        sender.setUserName(null);
        var message = new Message();
        message.setFrom(sender);
        var update = new Update();
        update.setMessage(message);
        var identity = new ExternalIdentity(IdentityProvider.TELEGRAM, "5000000001");
        var internalId = UUID.randomUUID();
        var identities = mock(UserIdentityService.class);
        var command = mock(CommandHandler.class);
        var client = mock(TelegramClient.class);
        when(identities.resolveOrRegister(identity, null)).thenReturn(internalId);
        when(command.canHandle("create")).thenReturn(true);
        when(command.handle(update, client, internalId)).thenReturn(Optional.empty());

        new MainCommandHandler(List.of(command), identities).handle(update, client, "create");

        verify(identities).resolveOrRegister(identity, null);
        verify(command).handle(update, client, internalId);
    }

    @Test
    void buttonUsesTheClickingUserRatherThanTheBotThatSentTheMessage() {
        var sender = new User(5_000_000_001L, "Player", false);
        var bot = new User(999L, "Bot", true);
        var message = new Message();
        message.setFrom(bot);
        var callback = new CallbackQuery();
        callback.setFrom(sender);
        callback.setMessage(message);
        var update = new Update();
        update.setCallbackQuery(callback);

        assertSame(sender, TelegramUtils.getUser(update));
    }
}
