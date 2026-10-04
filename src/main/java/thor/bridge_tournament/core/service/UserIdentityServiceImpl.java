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
        return transactionalManager.executeTransactional(() -> {
            var existingId = externalAccountRepository.findUserIdForUpdate(identity);
            if (existingId.isPresent()) {
                var user = userRepository.getById(existingId.get());
                if (!Objects.equals(user.username(), username)) {
                    userRepository.addUser(new UserDto(user.id(), username, user.name(), user.surname(), user.sportCategory()));
                }
                return user.id();
            }
            UUID userId = userRepository.addUser(new UserDto(null, username, null, null, 5.0));
            externalAccountRepository.link(identity, userId);
            return userId;
        });
    }
}
