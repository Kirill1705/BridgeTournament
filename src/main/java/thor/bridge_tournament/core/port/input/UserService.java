package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;

import java.util.List;
import java.util.UUID;

public interface UserService {
    /** Creates a user when the ID is null; otherwise updates that existing UUID. Never matches by username. */
    UUID register(UserDto playerDto);
    List<UserDto> getAllPlayers();

    List<UserDto> findByUsername(IdentityProvider provider, String username);
}
