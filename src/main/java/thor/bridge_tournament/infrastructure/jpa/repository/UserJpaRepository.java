package thor.bridge_tournament.infrastructure.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import thor.bridge_tournament.infrastructure.jpa.UserEntity;

import java.util.UUID;
import java.util.List;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    @Query(value = """
            SELECT u.* FROM users u JOIN external_accounts ea ON ea.user_id = u.id
            WHERE ea.provider = :provider AND u.removed = false
                AND lower(ltrim(btrim(ea.username), '@')) = lower(ltrim(btrim(:username), '@'))
            """, nativeQuery = true)
    List<UserEntity> findByProviderAndUsername(String provider, String username);

    @Query(value = """
            SELECT u.* FROM users u
            WHERE u.registration_provider = :provider
                AND lower(ltrim(btrim(u.username), '@')) = lower(ltrim(btrim(:username), '@'))
            FOR UPDATE
            """, nativeQuery = true)
    List<UserEntity> findByRegistrationProviderAndUsernameForUpdate(String provider, String username);
}
