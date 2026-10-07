package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import thor.bridge_tournament.core.domain.player.User;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;
import thor.bridge_tournament.core.mapping.UserMapper;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.input.UserService;
import thor.bridge_tournament.core.port.output.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UUID register(UserDto userDto) {
        if (userDto.id() != null) {
            userRepository.getById(userDto.id());
        }
        User user = UserMapper.fromDto(userDto);
        return userRepository.addUser(UserMapper.toDto(user));
    }

    @Override
    public List<UserDto> getAllPlayers() {
        return userRepository.allPlayers();
    }

    @Override
    public List<UserDto> findByUsername(IdentityProvider provider, String username) {
        return userRepository.findByUsername(provider, username);
    }
}
