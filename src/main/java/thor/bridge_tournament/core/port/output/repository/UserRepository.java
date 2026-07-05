package thor.bridge_tournament.core.port.output.repository;

import thor.bridge_tournament.core.port.dto.UserDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    UUID addUser(UserDto user);

    List<UserDto> allPlayers();

    Optional<UUID> getByUserName(String userName);

    UserDto getById(UUID uuid);

    List<UserDto> filterByIds(Collection<UUID> uuids);
}
