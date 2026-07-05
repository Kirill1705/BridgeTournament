package thor.bridge_tournament.infrastructure.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import thor.bridge_tournament.core.port.dto.movement.MovementDto;
import thor.bridge_tournament.core.port.output.repository.MovementRepository;
import thor.bridge_tournament.infrastructure.jpa.MovementEntity;
import thor.bridge_tournament.infrastructure.jpa.repository.MovementJpaRepository;
import thor.bridge_tournament.infrastructure.mapping.MovementMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class MovementRepositoryImpl implements MovementRepository {
    private final MovementJpaRepository repository;

    @Override
    public Optional<MovementDto> find(int pairsCount, List<Integer> roundsQuantities) {
        return repository.findMovement(pairsCount, roundsQuantities.toArray(new Integer[0]))
                .map(MovementMapper::toDto);
    }

    @Override
    public void save(MovementDto movementDto) {
        repository.save(MovementMapper.toEntity(movementDto));
    }

    @Override
    public Optional<UUID> find(int pairsCount, int roundsCount, String movementType) {
        return repository.findByPairsCountAndRoundsCountAndType(pairsCount, roundsCount, movementType).map(MovementEntity::getId);
    }

    @Override
    public void delete(UUID movementId) {
        repository.deleteById(movementId);
    }
}
