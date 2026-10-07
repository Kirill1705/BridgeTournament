package thor.infrastructure;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
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
import java.util.List;
import javax.sql.DataSource;
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
    @Autowired private DataSource dataSource;
    @Autowired private EntityManager entityManager;

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
        assertTrue(users.findByUsername(IdentityProvider.TELEGRAM, "old_name").isEmpty());
        assertEquals(id, users.findByUsername(IdentityProvider.TELEGRAM, "@NEW_NAME").getFirst().id());
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
        assertNull(jdbc.queryForObject("SELECT username FROM external_accounts WHERE user_id = ?", String.class, withoutUsername));
        assertNotEquals(withoutUsername, identities.resolveOrRegister(telegram(5_000_000_003L), null));
    }

    @Test
    void reusedUsernameDoesNotGrantAccessToAnotherAccountOrLegacyProfile() {
        UUID legacy = users.register(new UserDto(null, "shared_name", "Legacy", null, 5.0));
        UUID first = identities.resolveOrRegister(telegram(5_000_000_004L), "shared_name");
        int board = boards.addBoard(1);
        var firstEntry = entries.addBoardEntryImps(first, board, new RawBoardEntry("pass", null, null, 0), 0d);
        UUID second = identities.resolveOrRegister(telegram(5_000_000_005L), "shared_name");
        assertNotEquals(legacy, first);
        assertNotEquals(first, second);
        users.register(new UserDto(second, "shared_name", "Second", null, 3.0));
        assertEquals("Legacy", userRepository.getById(legacy).name());
        assertNull(userRepository.getById(first).name());
        assertNull(userRepository.getById(first).username());
        assertEquals("Second", userRepository.getById(second).name());
        assertEquals(List.of(second), users.findByUsername(IdentityProvider.TELEGRAM, "@SHARED_NAME").stream().map(UserDto::id).toList());
        assertEquals(first, identities.resolveOrRegister(telegram(5_000_000_004L), null));
        assertEquals(firstEntry.entryId(), entries.addBoardEntryImps(first, board, new RawBoardEntry("pass", null, null, 0), 0d).entryId());
    }

    @Test
    void uniqueIndexIsScopedByProviderCaseInsensitiveAndAllowsAbsentUsernames() {
        UUID first = users.register(new UserDto(null, null, null, null, 5.0));
        UUID second = users.register(new UserDto(null, null, null, null, 5.0));
        entityManager.flush();
        jdbc.update("INSERT INTO external_accounts(provider, external_id, user_id, username) VALUES ('TELEGRAM', 'unique1', ?, '@Thor1705')", first);
        jdbc.update("INSERT INTO external_accounts(provider, external_id, user_id, username) VALUES ('WEB', 'unique2', ?, 'thor1705')", second);
        jdbc.update("INSERT INTO external_accounts(provider, external_id, user_id, username) VALUES ('TELEGRAM', 'no_name1', ?, NULL)", first);
        jdbc.update("INSERT INTO external_accounts(provider, external_id, user_id, username) VALUES ('TELEGRAM', 'no_name2', ?, NULL)", second);
        assertEquals(first, users.findByUsername(IdentityProvider.TELEGRAM, "@tHoR1705").getFirst().id());
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "INSERT INTO external_accounts(provider, external_id, user_id, username) VALUES ('TELEGRAM', 'unique3', ?, 'THOR1705')", second));
    }

    @Test
    void migrationBackfillsLinkedUsernamesWithoutAutomaticallyLinkingLegacyProfiles() {
        UUID linked = users.register(new UserDto(null, " @Existing_Name ", "Current", null, 5.0));
        UUID legacy = users.register(new UserDto(null, "existing_name", "Legacy", null, 5.0));
        entityManager.flush();
        jdbc.execute("ALTER TABLE external_accounts DROP COLUMN username CASCADE");
        jdbc.update("INSERT INTO external_accounts(provider, external_id, user_id) VALUES ('TELEGRAM', 'migrated', ?)", linked);
        new ResourceDatabasePopulator(new ClassPathResource("db/changelog/changeset/004-provider-usernames.sql")).execute(dataSource);
        assertEquals("Existing_Name", jdbc.queryForObject("SELECT username FROM external_accounts WHERE user_id = ?", String.class, linked));
        assertEquals(List.of(linked), users.findByUsername(IdentityProvider.TELEGRAM, "@EXISTING_NAME").stream().map(UserDto::id).toList());
        assertEquals("existing_name", userRepository.getById(legacy).username());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM external_accounts WHERE user_id = ?", Integer.class, legacy));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentAccountsWithSameUsernameStayDistinctAndOnlyOneOwnsTheAlias() throws Exception {
        var firstIdentity = telegram(5_000_000_007L);
        var secondIdentity = telegram(5_000_000_008L);
        String username = "claimed_" + UUID.randomUUID();
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                ready.countDown();
                assertTrue(start.await(10, TimeUnit.SECONDS));
                return identities.resolveOrRegister(firstIdentity, username);
            });
            var second = executor.submit(() -> {
                ready.countDown();
                assertTrue(start.await(10, TimeUnit.SECONDS));
                return identities.resolveOrRegister(secondIdentity, username);
            });
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            UUID firstId = first.get(15, TimeUnit.SECONDS);
            UUID secondId = second.get(15, TimeUnit.SECONDS);
            assertNotEquals(firstId, secondId);
            assertEquals(1, users.findByUsername(IdentityProvider.TELEGRAM, username).size());
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM users WHERE username = ?", Integer.class, username));
            assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM external_accounts WHERE provider = 'TELEGRAM' AND external_id IN (?, ?)",
                    Integer.class, firstIdentity.externalId(), secondIdentity.externalId()));
        } finally {
            var created = jdbc.query("SELECT user_id FROM external_accounts WHERE provider = 'TELEGRAM' AND external_id IN (?, ?)",
                    (rs, row) -> rs.getObject("user_id", UUID.class), firstIdentity.externalId(), secondIdentity.externalId());
            jdbc.update("DELETE FROM external_accounts WHERE provider = 'TELEGRAM' AND external_id IN (?, ?)", firstIdentity.externalId(), secondIdentity.externalId());
            created.forEach(id -> jdbc.update("DELETE FROM users WHERE id = ?", id));
        }
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
