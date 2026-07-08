package thor.bridge_tournament.core.port.dto.board;

import java.util.Map;
import java.util.UUID;

public record PairBoardResult(UUID entryId, String countType, int points, double duplicatePoints, Map<BoardEntryDto, Double> protocol) {
}
