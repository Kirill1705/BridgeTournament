package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.domain.identity.ExternalIdentity;

import java.util.UUID;

public interface UserIdentityService {
    /** Called by a trusted adapter after identifying the external account. Username is display metadata only. */
    UUID resolveOrRegister(ExternalIdentity identity, String username);
}
