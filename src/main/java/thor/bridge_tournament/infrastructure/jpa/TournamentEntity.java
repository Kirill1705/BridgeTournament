package thor.bridge_tournament.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@NoArgsConstructor
public class TournamentEntity extends BaseEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "count_type")
    private String countType;

    @Column(name = "boards")
    private int boards;

    @Column(name = "rounds")
    private Integer rounds;

    @Column(name = "name")
    private String name;

    @Column(name = "started")
    private boolean started;

    @ElementCollection
    @CollectionTable(
            name = "tournament_tds",
            joinColumns = @JoinColumn(name = "tournament_id")
    )
    @Column(name = "td_id")
    private List<UUID> tds;
}
