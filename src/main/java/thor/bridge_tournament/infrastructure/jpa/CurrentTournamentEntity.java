package thor.bridge_tournament.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Entity
@Table(name = "users_current")
@Getter
@Setter
@DynamicUpdate
@AllArgsConstructor
@NoArgsConstructor
public class CurrentTournamentEntity {
    @Id
    @Column(name = "user_id")
    private UUID id;
    
    @Column(name = "td")
    private UUID tdId;
    
    @Column(name = "player")
    private UUID playerId;
}
