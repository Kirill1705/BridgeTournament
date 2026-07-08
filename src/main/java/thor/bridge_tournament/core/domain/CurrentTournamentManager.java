package thor.bridge_tournament.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import thor.bridge_tournament.core.domain.player.SportCategory;
import thor.bridge_tournament.core.domain.player.User;
import thor.bridge_tournament.core.domain.tournament.Tournament;
import thor.bridge_tournament.core.exception.TournamentNotFoundException;
import thor.bridge_tournament.core.mapping.TournamentMapper;
import thor.bridge_tournament.core.mapping.UserMapper;
import thor.bridge_tournament.core.port.output.repository.CurrentTournamentRepository;
import thor.bridge_tournament.core.port.output.repository.TournamentRepository;
import thor.bridge_tournament.core.port.output.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CurrentTournamentManager {
    private final UserRepository userRepository;
    private final CurrentTournamentRepository currentTournamentRepository;
    private final TournamentRepository tournamentRepository;

    public Tournament getByTd(String tdUserName) {
        UUID userId = getUserIdOrRegister(tdUserName);
        Optional<UUID> tournamentId = currentTournamentRepository.getTournamentIdByTd(userId);
        if (tournamentId.isEmpty()) {
            throw new TournamentNotFoundException(userId);
        }
        return TournamentMapper.fromDto(tournamentRepository.getById(tournamentId.get()));
    }

    public Tournament getByPlayerId(String playerUserName) {
        UUID userId = getUserIdOrRegister(playerUserName);
        Optional<UUID> tournamentId = currentTournamentRepository.getTournamentIdByPlayer(userId);
        if (tournamentId.isEmpty()) {
            throw new TournamentNotFoundException(userId);
        }
        return TournamentMapper.fromDto(tournamentRepository.getById(tournamentId.get()));
    }

    public UUID getUserIdOrRegister(String userName) {
        Optional<UUID> uuid = userRepository.getByUserName(userName);
        if (uuid.isEmpty()) {
            User user = new User(null, null, null, new SportCategory(5.0), userName);
            return userRepository.addUser(UserMapper.toDto(user));
        }
        return uuid.get();
    }
}
