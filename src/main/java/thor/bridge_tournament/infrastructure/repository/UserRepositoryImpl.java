package thor.bridge_tournament.infrastructure.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.output.repository.UserRepository;
import thor.bridge_tournament.infrastructure.jpa.UserEntity;
import thor.bridge_tournament.infrastructure.jpa.repository.UserJpaRepository;
import thor.bridge_tournament.infrastructure.mapping.UserMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository repository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UUID addUser(UserDto user) {
        return repository.save(mapper.toJpa(user)).getId();
    }

    @Override
    public List<UserDto> allPlayers() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public Optional<UUID> getByUserName(String userName) {
        return repository.findByUsername(userName).map(UserEntity::getId);
    }

    @Override
    public UserDto getById(UUID uuid) {
        return mapper.toDto(repository.findById(uuid).get());
    }

    @Override
    public List<UserDto> filterByIds(Collection<UUID> uuids) {
        return repository.findAllById(uuids).stream().map(mapper::toDto).toList();
    }
}
