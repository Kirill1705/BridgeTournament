package thor.bridge_tournament.core.domain.tournament;

import thor.bridge_tournament.core.port.dto.tournament.PairDto;

public record PairWithResult(PairDto pair, double result) {
}
