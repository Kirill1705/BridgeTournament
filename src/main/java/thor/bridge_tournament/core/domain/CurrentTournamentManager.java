package thor.bridge_tournament.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import thor.bridge_tournament.core.domain.tournament.Tournament;
import thor.bridge_tournament.core.exception.TournamentNotFoundException;
import thor.bridge_tournament.core.mapping.TournamentMapper;
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

    public Tournament getByTd(UUID userId) {
        Optional<UUID> tournamentId = currentTournamentRepository.getTournamentIdByTd(userId);
        if (tournamentId.isEmpty()) {
            throw new TournamentNotFoundException(userId);
        }
        return TournamentMapper.fromDto(tournamentRepository.getById(tournamentId.get()));
    }

    public Tournament getByPlayerId(UUID userId) {
        Optional<UUID> tournamentId = currentTournamentRepository.getTournamentIdByPlayer(userId);
        if (tournamentId.isEmpty()) {
            throw new TournamentNotFoundException(userId);
        }
        return TournamentMapper.fromDto(tournamentRepository.getById(tournamentId.get()));
    }
}
