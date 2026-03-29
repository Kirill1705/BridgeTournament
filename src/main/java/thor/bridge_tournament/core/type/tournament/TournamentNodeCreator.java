package thor.bridge_tournament.core.type.tournament;

import thor.bridge_tournament.core.type.board.BoardEntry;
import thor.bridge_tournament.core.type.player.Pair;

import java.util.List;

public interface TournamentNodeCreator {
    TournamentNode create(List<BoardEntry> boards, int table, int round);
}
