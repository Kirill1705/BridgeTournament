package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import thor.bridge_tournament.infrastructure.jpa.MovementEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovementJpaRepository extends JpaRepository<MovementEntity, UUID> {
    @Query(value = """
        SELECT m.* FROM movements m
        JOIN UNNEST(:rounds) WITH ORDINALITY AS r(value, idx)
        ON m.rounds_count = r.value
        WHERE m.pairs_count = :pairsCount
        ORDER BY r.idx
        LIMIT 1
        """, nativeQuery = true)
    Optional<MovementEntity> findMovement(int pairsCount, Integer[] rounds);

    Optional<MovementEntity> findByPairsCountAndRoundsCountAndType(int pairsCount, int roundsCount, String type);
}
