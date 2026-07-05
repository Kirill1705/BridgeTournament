package thor.bridge_tournament.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "board_entries")
@Getter
@Setter
@NoArgsConstructor
public class BoardEntryEntity extends BaseEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "board_id")
    private BoardEntity board;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ns")
    private PairEntity ns;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ew")
    private PairEntity ew;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "writer_id")
    private UserEntity writer;

    @Column(name = "contract")
    private String contract;

    @Column(name = "declarer")
    private String declarer;

    @Column(name = "lead")
    private String lead;

    @Column(name = "result")
    private int result;

}
