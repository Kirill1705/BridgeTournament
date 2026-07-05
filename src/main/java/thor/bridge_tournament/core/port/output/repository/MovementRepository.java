package thor.bridge_tournament.core.port.output.repository;

import thor.bridge_tournament.core.port.dto.movement.MovementDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovementRepository {
    Optional<MovementDto> find(int pairsCount, List<Integer> roundsQuantities);

    void save(MovementDto movementDto);

    Optional<UUID> find(int pairsCount, int roundsCount, String movementType);

    void delete(UUID uuid);
}
