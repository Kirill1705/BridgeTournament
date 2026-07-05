package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.UserDto;

import java.util.List;

public interface UserService {
    void register(UserDto playerDto);
    List<UserDto> getAllPlayers();
}
