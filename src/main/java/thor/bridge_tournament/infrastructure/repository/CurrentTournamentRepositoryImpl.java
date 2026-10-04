package thor.bridge_tournament.infrastructure.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.core.port.output.repository.CurrentTournamentRepository;
import thor.bridge_tournament.infrastructure.jpa.CurrentTournamentEntity;
import thor.bridge_tournament.infrastructure.jpa.repository.CurrentTournamentJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class CurrentTournamentRepositoryImpl implements CurrentTournamentRepository {
    private final CurrentTournamentJpaRepository repository;
    
    @Override
    public Optional<UUID> getTournamentIdByTd(UUID tdId) {
        return repository.findById(tdId).map(CurrentTournamentEntity::getTdId);
    }

    @Override
    public Optional<UUID> getTournamentIdByPlayer(UUID playerId) {
        return repository.findById(playerId).map(CurrentTournamentEntity::getPlayerId);
    }

    @Override
    @Transactional
    public void switchTd(UUID tournamentId, UUID tdId) {
        CurrentTournamentEntity current = repository.findById(tdId)
                .orElseGet(() -> new CurrentTournamentEntity(tdId, null, null));
        current.setTdId(tournamentId);
        repository.save(current);
    }

    @Override
    @Transactional
    public void switchPlayer(UUID tournamentId, UUID playerId) {
        CurrentTournamentEntity current = repository.findById(playerId)
                .orElseGet(() -> new CurrentTournamentEntity(playerId, null, null));
        current.setPlayerId(tournamentId);
        repository.save(current);
    }
}
