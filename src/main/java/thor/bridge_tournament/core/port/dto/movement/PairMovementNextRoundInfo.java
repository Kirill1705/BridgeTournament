package thor.bridge_tournament.core.port.dto.movement;

import java.util.List;

public record PairMovementNextRoundInfo(PairMovementEntryDto movementEntry, List<Integer> dealsNotPlayed) {
}
