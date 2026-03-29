package thor.bridge_tournament.core.type.tournament;

import thor.bridge_tournament.core.type.board.BoardEntry;

import java.util.List;
import java.util.UUID;

public class TournamentNodeCreatorImpl implements TournamentNodeCreator {
    private final UUID tournamentId;

    public TournamentNodeCreatorImpl(UUID tournamentId) {
        this.tournamentId = tournamentId;
    }

    @Override
    public TournamentNode create(List<BoardEntry> boards, int table, int round) {
        return new TournamentNode(UUID.randomUUID(), tournamentId, round, table, boards);
    }
}
