package thor.bridge_tournament.infrastructure.repository;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.domain.identity.IdentityProvider;
import thor.bridge_tournament.core.port.output.repository.UserRepository;
import thor.bridge_tournament.infrastructure.jpa.repository.UserJpaRepository;
import thor.bridge_tournament.infrastructure.mapping.UserMapper;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository repository;
    private final UserMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public UUID addUser(UserDto user) {
        var entity = mapper.toJpa(user);
        if (user.id() != null) {
            entity.setRegistrationProvider(repository.findById(user.id()).orElseThrow().getRegistrationProvider());
        }
        return repository.save(entity).getId();
    }

    @Override
    @Transactional
    public UUID addUser(UserDto user, IdentityProvider provider) {
        var entity = mapper.toJpa(user);
        entity.setRegistrationProvider(provider.name());
        return repository.save(entity).getId();
    }

    @Override
    @Transactional
    public void releaseUsername(IdentityProvider provider, String username, UUID exceptUserId) {
        jdbcTemplate.queryForObject("SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(?, 0))",
                Integer.class, "usernames:" + provider.name());
        if (username != null) {
            for (var user : repository.findByRegistrationProviderAndUsernameForUpdate(provider.name(), username)) {
                if (!user.getId().equals(exceptUserId)) {
                    user.setUsername(null);
                }
            }
            // Release the unique key before saving the incoming account's username.
            repository.flush();
        }
    }

    @Override
    public List<UserDto> allPlayers() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public UserDto getById(UUID uuid) {
        return mapper.toDto(repository.findById(uuid).get());
    }

    @Override
    public List<UserDto> filterByIds(Collection<UUID> uuids) {
        return repository.findAllById(uuids).stream().map(mapper::toDto).toList();
    }

    @Override
    public List<UserDto> findByUsername(IdentityProvider provider, String username) {
        return repository.findByProviderAndUsername(provider.name(), username).stream().map(mapper::toDto).toList();
    }
}
