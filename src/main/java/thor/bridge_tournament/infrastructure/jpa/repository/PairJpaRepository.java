package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import thor.bridge_tournament.infrastructure.jpa.PairEntity;

import java.util.List;
import java.util.UUID;

public interface PairJpaRepository extends JpaRepository<PairEntity, UUID> {
    @Override
    @EntityGraph(attributePaths = {"firstPlayer", "secondPlayer"})
    List<PairEntity> findAllById(Iterable<UUID> ids);
}
