package thor.bridge_tournament.core.port.output.repository;

import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserRepository {
    UUID addUser(UserDto user);

    UUID addUser(UserDto user, IdentityProvider provider);

    void releaseUsername(IdentityProvider provider, String username, UUID exceptUserId);

    List<UserDto> allPlayers();

    UserDto getById(UUID uuid);

    List<UserDto> filterByIds(Collection<UUID> uuids);

    List<UserDto> findByUsername(IdentityProvider provider, String username);
}
