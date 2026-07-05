package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thor.bridge_tournament.infrastructure.jpa.PairEntity;

import java.util.UUID;

public interface PairJpaRepository extends JpaRepository<PairEntity, UUID> {
}
