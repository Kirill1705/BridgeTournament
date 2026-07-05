package thor.bridge_tournament.core.mapping;

import thor.bridge_tournament.core.domain.movement.Movement;
import thor.bridge_tournament.core.domain.tournament.MovementType;
import thor.bridge_tournament.core.port.dto.movement.MovementDto;

public class MovementMapper {
    public static MovementDto toDto(Movement movement) {
        return new MovementDto(
                movement.getType().name().toLowerCase(),
                movement.getRoundsCount(),
                movement.getPairsCount(),
                movement.getBody()
        );
    }

    public static Movement fromDto(MovementDto dto) {
        return new Movement(
                MovementType.valueOf(dto.type().toUpperCase()),
                dto.pairsCount(),
                dto.roundsCount(),
                dto.body()
        );
    }
}
