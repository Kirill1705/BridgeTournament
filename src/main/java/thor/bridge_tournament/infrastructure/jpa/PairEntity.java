package thor.bridge_tournament.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "pairs")
@Getter
@Setter
@NoArgsConstructor
public class PairEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "first_player_id")
    private UserEntity firstPlayer;

    @ManyToOne
    @JoinColumn(name = "second_player_id")
    private UserEntity secondPlayer;
}
