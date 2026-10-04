package thor.bridge_tournament.core.domain.identity;

import java.util.Objects;

public record ExternalIdentity(IdentityProvider provider, String externalId) {
    public ExternalIdentity {
        Objects.requireNonNull(provider, "Identity provider is required");
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("External user ID is required");
        }
    }
}
