package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thor.bridge_tournament.infrastructure.jpa.CurrentTournamentEntity;

import java.util.UUID;

public interface CurrentTournamentJpaRepository extends JpaRepository<CurrentTournamentEntity, UUID> {

}
