package thor.bridge_tournament.infrastructure.repository;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.core.domain.identity.ExternalIdentity;
import thor.bridge_tournament.core.port.output.repository.ExternalAccountRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ExternalAccountRepositoryImpl implements ExternalAccountRepository {
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    @Override
    public Optional<UUID> findUserIdForUpdate(ExternalIdentity identity) {
        // A row lock cannot protect a first login: there is no account row to lock yet.
        jdbcTemplate.queryForObject("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(?, 0))",
                Integer.class, identity.provider().name() + ":" + identity.externalId());
        return jdbcTemplate.query("SELECT user_id FROM external_accounts WHERE provider = ? AND external_id = ?",
                (rs, row) -> rs.getObject("user_id", UUID.class), identity.provider().name(), identity.externalId())
                .stream().findFirst();
    }

    @Override
    public void link(ExternalIdentity identity, UUID userId) {
        // Flush a newly registered JPA user before inserting the referencing account through JDBC.
        entityManager.flush();
        jdbcTemplate.update("INSERT INTO external_accounts(provider, external_id, user_id) VALUES (?, ?, ?)",
                identity.provider().name(), identity.externalId(), userId);
    }
}
