package thor.bridge_tournament.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tournament_nodes")
@NoArgsConstructor
@Getter
@Setter
public class TournamentNodeEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "tournament_id")
    private UUID tournamentId;

    @Column(name = "round")
    private int round;

    @Column(name = "table")
    private int table;

    @ManyToOne
    @JoinColumn(name = "ns")
    private PairEntity ns;

    @ManyToOne
    @JoinColumn(name = "ew")
    private PairEntity ew;

    @ElementCollection
    @CollectionTable(
            name = "tournament_node_boards",
            joinColumns = @JoinColumn(name = "node_id")
    )
    @Column(name = "board_number")
    private List<Integer> boards;
}
