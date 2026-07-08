package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import thor.bridge_tournament.core.domain.player.User;
import thor.bridge_tournament.core.mapping.UserMapper;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.core.port.output.TransactionalManager;
import thor.bridge_tournament.core.port.output.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public void register(UserDto userDto) {
        User user = UserMapper.fromDto(userDto);
        Optional<UUID> userId = userRepository.getByUserName(user.username());
        userId.ifPresent(user::setUuid);
        userRepository.addUser(UserMapper.toDto(user));
    }

    @Override
    public List<UserDto> getAllPlayers() {
        return userRepository.allPlayers();
    }
}
