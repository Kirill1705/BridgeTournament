package thor.bridge_tournament.infrastructure.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.output.repository.PairRepository;
import thor.bridge_tournament.infrastructure.jpa.repository.PairJpaRepository;
import thor.bridge_tournament.infrastructure.mapping.PairMapper;

import java.util.List;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class PairRepositoryImpl implements PairRepository {
    private final PairJpaRepository repository;
    private final PairMapper mapper;

    @Override
    @Transactional
    public UUID addPair(PairDto pair) {
        return repository.save(mapper.toJpa(pair)).getId();
    }

    @Override
    public List<PairDto> filterByIds(List<UUID> uuids) {
        return repository.findAllById(uuids).stream().map(mapper::toDto).toList();
    }
}
