package thor.bridge_tournament.core.port.output.repository;

import thor.bridge_tournament.core.domain.identity.ExternalIdentity;

import java.util.Optional;
import java.util.UUID;

public interface ExternalAccountRepository {
    /** Serializes registration of this identity, including when it does not exist yet, until transaction completion. */
    Optional<UUID> findUserIdForUpdate(ExternalIdentity identity);

    void link(ExternalIdentity identity, UUID userId);
}
