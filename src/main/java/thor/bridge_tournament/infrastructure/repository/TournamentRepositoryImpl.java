package thor.bridge_tournament.infrastructure.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.core.port.dto.tournament.TournamentDto;
import thor.bridge_tournament.core.port.output.repository.TournamentRepository;
import thor.bridge_tournament.infrastructure.jpa.repository.TournamentJpaRepository;
import thor.bridge_tournament.infrastructure.mapping.TournamentMapper;

import java.util.List;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class TournamentRepositoryImpl implements TournamentRepository {
    private final TournamentJpaRepository repository;
    private final TournamentMapper mapper;

    @Override
    @Transactional
    public UUID save(TournamentDto tournamentDto) {
        return repository.save(mapper.toJpa(tournamentDto)).getId();
    }

    @Override
    public TournamentDto getById(UUID uuid) {
        return mapper.toDto(repository.findById(uuid).get());
    }

    @Override
    @Transactional
    public void addPlayerWithoutPair(UUID playerId, UUID tournamentId) {
        repository.addPlayerToTournament(tournamentId, playerId);
    }

    @Override
    @Transactional
    public void addPair(UUID pair, UUID tournamentId) {
        repository.addPairToTournament(tournamentId, pair);
    }

    @Override
    public List<UUID> getPlayersWithoutPair(UUID tournamentId) {
        return repository.getAllPlayers(tournamentId);
    }

    @Override
    public List<UUID> getAllPairs(UUID tournamentId) {
        return repository.getAllPairs(tournamentId);
    }

    @Override
    @Transactional
    public void removePlayersWithOutPairs(UUID tournamentId) {
        repository.removeAllPlayers(tournamentId);
    }
}
