package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import thor.bridge_tournament.core.domain.identity.ExternalIdentity;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.input.UserIdentityService;
import thor.bridge_tournament.core.port.output.TransactionalManager;
import thor.bridge_tournament.core.port.output.repository.ExternalAccountRepository;
import thor.bridge_tournament.core.port.output.repository.UserRepository;

import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
public class UserIdentityServiceImpl implements UserIdentityService {
    private final ExternalAccountRepository externalAccountRepository;
    private final UserRepository userRepository;
    private final TransactionalManager transactionalManager;

    @Override
    public UUID resolveOrRegister(ExternalIdentity identity, String username) {
        Objects.requireNonNull(identity, "External identity is required");
        String normalizedUsername = username == null ? null : username.strip().replaceFirst("^@+", "");
        String currentUsername = normalizedUsername == null || normalizedUsername.isBlank() ? null : normalizedUsername;
        return transactionalManager.executeTransactional(() -> {
            var existingId = externalAccountRepository.findUserIdForUpdate(identity);
            if (existingId.isPresent()) {
                var user = userRepository.getById(existingId.get());
                userRepository.releaseUsername(identity.provider(), currentUsername, user.id());
                if (!Objects.equals(user.username(), currentUsername)) {
                    userRepository.addUser(new UserDto(user.id(), currentUsername, user.name(), user.surname(), user.sportCategory()));
                }
                updateUsername(identity, currentUsername, user.id());
                return user.id();
            }
            userRepository.releaseUsername(identity.provider(), currentUsername, null);
            UUID userId = userRepository.addUser(new UserDto(null, currentUsername, null, null, 5.0), identity.provider());
            externalAccountRepository.link(identity, userId);
            updateUsername(identity, currentUsername, userId);
            return userId;
        });
    }

    private void updateUsername(ExternalIdentity identity, String username, UUID userId) {
        var displaced = externalAccountRepository.updateUsername(identity, username);
        for (var user : userRepository.filterByIds(displaced)) {
            if (!user.id().equals(userId) && user.username() != null
                    && user.username().strip().replaceFirst("^@+", "").equalsIgnoreCase(username)) {
                userRepository.addUser(new UserDto(user.id(), null, user.name(), user.surname(), user.sportCategory()));
            }
        }
    }
}
