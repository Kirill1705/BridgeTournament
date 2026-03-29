package thor.bridge_tournament.core.type.tournament;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class TournamentMember {
    private final UUID uuid;
    private final UUID pairId;
    private final int number;
    private final UUID tournamentId;
    @Setter
    private double rate;

    public TournamentMember(UUID uuid, UUID pairId, int number, UUID tournamentId) {
        this.uuid = uuid;
        this.pairId = pairId;
        this.number = number;
        this.tournamentId = tournamentId;
    }
}
