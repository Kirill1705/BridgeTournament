package thor.bridge_tournament.infrastructure.jpa.dummy;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "tournament_node_entries")
@Getter
@Setter
public class TournamentNodeEntryEntity {
    @EmbeddedId
    private TournamentNodeEntryEntityId id;

    @Embeddable
    public record TournamentNodeEntryEntityId(
        @Column(name = "node_id") UUID nodeId,
        @Column(name = "board_entry_id") UUID boardEntryId
    ) implements Serializable {}
}
