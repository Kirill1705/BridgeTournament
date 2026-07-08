package thor.bridge_tournament.core.domain.match;

import lombok.AllArgsConstructor;
import lombok.Getter;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Team {
    private final UUID uuid;

    private final PairDto firstPair;

    private final PairDto secondPair;

    private final String name;
}
