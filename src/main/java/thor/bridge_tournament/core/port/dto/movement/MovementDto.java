package thor.bridge_tournament.core.port.dto.movement;

import thor.bridge_tournament.core.domain.movement.MovementBodyNode;

import java.util.List;

public record MovementDto(
        String type,
        int roundsCount,
        int pairsCount,
        List<MovementBodyNode> body
) {
}
