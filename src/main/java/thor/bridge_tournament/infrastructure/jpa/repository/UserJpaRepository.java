package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thor.bridge_tournament.infrastructure.jpa.UserEntity;

import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
}
