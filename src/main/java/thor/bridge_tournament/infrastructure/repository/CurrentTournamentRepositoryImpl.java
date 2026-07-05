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
        repository.save(new CurrentTournamentEntity(tdId, tournamentId, null));
    }

    @Override
    @Transactional
    public void switchPlayer(UUID tournamentId, UUID playerId) {
        repository.save(new CurrentTournamentEntity(playerId, null, tournamentId));
    }
}
