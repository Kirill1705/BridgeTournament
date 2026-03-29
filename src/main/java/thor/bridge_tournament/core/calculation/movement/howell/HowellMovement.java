package thor.bridge_tournament.core.calculation.movement.howell;

import thor.bridge_tournament.core.calculation.movement.BoardsSelector;
import thor.bridge_tournament.core.calculation.movement.Movement;
import thor.bridge_tournament.core.type.board.BoardEntry;
import thor.bridge_tournament.core.type.player.Pair;
import thor.bridge_tournament.core.type.tournament.TournamentNode;
import thor.bridge_tournament.core.type.tournament.TournamentNodeCreator;

import java.util.*;

public class HowellMovement implements Movement {
    @Override
    public Collection<TournamentNode> create(BoardsSelector selector, List<Pair> pairs, TournamentNodeCreator creator) {
        List<BoardWithPairs> boards = createPairs(pairs);
        int roundsCount = pairs.size() - 2;
        for (int i = 0; i < roundsCount; i++) {
            int finalI = i;
            List<BoardWithPairs> realBoards = boards.stream()
                    .filter(board -> board.board() != finalI)
                    .toList();
            HowellData data = new HowellData(pairs.size() / 2, roundsCount);
            if (!find(data, realBoards, roundsCount)) {
                System.out.println("Go to next round");
                continue;
            }
            SortedSet<HowellNode> result = data.result();
            return result.stream()
                    .map(howellNode -> creator.create(selector.select(howellNode.board())
                            .stream()
                            .map(board -> new BoardEntry(board, pairs.get(howellNode.first()), pairs.get(howellNode.second())))
                            .toList(), howellNode.table(), howellNode.round()))
                    .toList();
        }
        throw new RuntimeException();
    }

    private boolean find(HowellData data, List<BoardWithPairs> boards, int roundCount) {
        int round = data.getSize() / data.getTablesCount();
        if (round >= roundCount)
            return true;
        for (BoardWithPairs board: boards) {
            int table = data.getSize() % data.getTablesCount();
            if (!data.contains(board, round, table)) {
                data.add(board, round, table);
                if (find(data, boards, roundCount)) {
                    return true;
                }
                data.remove(board, round, table);
            }
        }
        return false;
    }

    private List<BoardWithPairs> createPairs(List<Pair> pairs) {
        List<BoardWithPairs> boards = new ArrayList<>();
        int n = pairs.size()/2;
        for (int r = 0; r < 2*n - 1; r++) {
            int s = r + 1;
            boards.add(new BoardWithPairs(r, 0, s));
            List<Integer> others = new ArrayList<>();
            for (int i = s + 1; i <= 2*n - 1; i++) {
                others.add(i);
            }
            for (int i = 1; i <= s - 1; i++) {
                others.add(i);
            }
            for (int t = 1; t <= n - 1; t++) {
                int first = others.get(t - 1);
                int second = others.get(others.size() - t);
                boards.add(new BoardWithPairs(r, first, second));
            }
        }
        return boards;
    }

    private record BoardWithRound(int round, int table, int board, int first, int second) implements Comparable<BoardWithRound> {

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (BoardWithRound) obj;
            return this.round == that.round && (
                    this.board == that.board ||
                    this.first == that.first ||
                    this.first == that.second ||
                    this.second == that.first ||
                    this.second == that.second
            ) || this.board == that.board && this.first == that.first && this.second == that.second;
        }

        @Override
        public int compareTo(BoardWithRound o) {
            if (this.equals(o))
                return 0;
            return Comparator.comparing(BoardWithRound::round)
                    .thenComparing(BoardWithRound::table)
                    .thenComparing(BoardWithRound::board)
                    .thenComparing(BoardWithRound::first)
                    .compare(this, o);
        }
    }
}
