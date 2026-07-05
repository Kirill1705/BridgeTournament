package thor.bridge_tournament.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "boards")
@Getter
@Setter
@NoArgsConstructor
public class BoardEntity extends BaseEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "number")
    private int number;

    @Column(name = "dealer")
    private String dealer;

    @Column(name = "vulnerable")
    private String vulnerable;
}
