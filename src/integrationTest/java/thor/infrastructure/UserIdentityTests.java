package thor.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import thor.bridge_tournament.core.domain.identity.ExternalIdentity;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.input.*;
import thor.bridge_tournament.core.port.output.repository.UserRepository;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = thor.bridge_tournament.BridgeTournamentApplication.class)
@Testcontainers
@ActiveProfiles("test")
@ComponentScan(basePackages = {"thor.bridge_tournament.core.service", "thor.bridge_tournament.infrastructure"})
class UserIdentityTests {
    @Autowired private UserIdentityService identities;
    @Autowired private UserService users;
    @Autowired private UserRepository userRepository;
    @Autowired private BoardService boards;
    @Autowired private BoardEntryService entries;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void usernameChangePreservesUserProfileAndBoardEntryOwnership() {
        var identity = telegram(5_000_000_001L);
        UUID id = identities.resolveOrRegister(identity, "old_name");
        users.register(new UserDto(id, "old_name", "Name", "Surname", 2.5));
        int board = boards.addBoard(1);
        var first = entries.addBoardEntryImps(id, board, new RawBoardEntry("4S", "N", "cK", 0), 0d);

        UUID renamed = identities.resolveOrRegister(identity, "new_name");
        assertEquals(id, renamed);
        var profile = userRepository.getById(renamed);
        assertEquals("new_name", profile.username());
        assertEquals("Name", profile.name());
        assertEquals("Surname", profile.surname());
        assertEquals(2.5, profile.sportCategory());
        var updated = entries.addBoardEntryImps(renamed, board, new RawBoardEntry("4S", "N", "cK", 1), 0d);
        assertEquals(first.entryId(), updated.entryId());
        assertEquals(1, updated.protocol().size());
    }

    @Test
    void supportsAbsentAndRemovedUsernameWithoutMergingAccounts() {
        var identity = telegram(5_000_000_002L);
        UUID withoutUsername = identities.resolveOrRegister(identity, null);
        assertEquals(withoutUsername, identities.resolveOrRegister(identity, "added_later"));
        assertEquals(withoutUsername, identities.resolveOrRegister(identity, null));
        assertNull(userRepository.getById(withoutUsername).username());
        assertNotEquals(withoutUsername, identities.resolveOrRegister(telegram(5_000_000_003L), null));
    }

    @Test
    void reusedUsernameDoesNotGrantAccessToAnotherAccountOrLegacyProfile() {
        UUID legacy = users.register(new UserDto(null, "shared_name", "Legacy", null, 5.0));
        UUID first = identities.resolveOrRegister(telegram(5_000_000_004L), "shared_name");
        UUID second = identities.resolveOrRegister(telegram(5_000_000_005L), "shared_name");
        assertNotEquals(legacy, first);
        assertNotEquals(first, second);
        users.register(new UserDto(second, "shared_name", "Second", null, 3.0));
        assertEquals("Legacy", userRepository.getById(legacy).name());
        assertNull(userRepository.getById(first).name());
        assertEquals("Second", userRepository.getById(second).name());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentFirstLoginsCreateExactlyOneUserAndAccount() throws Exception {
        var identity = telegram(5_000_000_006L);
        String username = "concurrent_" + UUID.randomUUID();
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var task = (java.util.concurrent.Callable<UUID>) () -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Login start timed out");
                return identities.resolveOrRegister(identity, username);
            };
            var first = executor.submit(task);
            var second = executor.submit(task);
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            UUID created = first.get(15, TimeUnit.SECONDS);
            assertEquals(created, second.get(15, TimeUnit.SECONDS));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM users WHERE username = ?", Integer.class, username));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM external_accounts WHERE provider = 'TELEGRAM' AND external_id = ?", Integer.class, identity.externalId()));
        } finally {
            jdbc.update("DELETE FROM external_accounts WHERE provider = 'TELEGRAM' AND external_id = ?", identity.externalId());
            jdbc.update("DELETE FROM users WHERE username = ?", username);
        }
    }

    private ExternalIdentity telegram(long id) {
        return new ExternalIdentity(IdentityProvider.TELEGRAM, Long.toString(id));
    }
}
