package thor.bridge_tournament.core.port.output.repository;

import thor.bridge_tournament.core.port.dto.tournament.PairDto;

import java.util.List;
import java.util.UUID;

public interface PairRepository {
    UUID addPair(PairDto pair);

    List<PairDto> filterByIds(List<UUID> uuids);
}
