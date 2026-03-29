package thor.bridge_tournament.core.type.tournament;

import lombok.AllArgsConstructor;
import lombok.Getter;
import thor.bridge_tournament.core.type.board.BoardEntry;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TournamentNode {
    private final UUID uuid;
    private final UUID tournamentId;
    private final int round;
    private final int table;
    private final List<BoardEntry> boards;
}
