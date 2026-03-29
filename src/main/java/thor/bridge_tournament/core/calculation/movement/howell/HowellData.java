package thor.bridge_tournament.core.calculation.movement.howell;

import lombok.Getter;

import java.util.*;

public class HowellData {
    @Getter
    private final HashMap<BoardWithPairs, RoundTable> data;
    private final HashSet<Integer>[] usedPairs;
    private final HashSet<Integer>[] usedBoards;

    @Getter
    private final int tablesCount;

    @Getter
    private int size = 0;

    public HowellData(int tablesCount, int roundCount) {
        this.tablesCount = tablesCount;
        data = new HashMap<>();
        usedPairs = new HashSet[roundCount];
        usedBoards = new HashSet[roundCount];
        for (int i = 0; i < roundCount; i++) {
            usedBoards[i] = new HashSet<>();
            usedPairs[i] = new HashSet<>();
        }
    }

    public boolean contains(BoardWithPairs board, int round, int table) {
        return data.containsKey(board) || usedPairs[round].contains(board.first()) || usedPairs[round].contains(board.second()) || usedBoards[round].contains(board.board());
    }

    public void add(BoardWithPairs board, int round, int table) {
        data.put(board, new RoundTable(round, table));
        usedPairs[round].add(board.first());
        usedPairs[round].add(board.second());
        usedBoards[round].add(board.board());
        size++;
    }

    public void remove(BoardWithPairs board, int round, int table) {
        data.remove(board);
        usedPairs[round].remove(board.first());
        usedPairs[round].remove(board.second());
        usedBoards[round].remove(board.board());
        size--;
    }

    public SortedSet<HowellNode> result() {
        SortedSet<HowellNode> result = new TreeSet<>();
        for (Map.Entry<BoardWithPairs, RoundTable> entry: data.entrySet()) {
            result.add(new HowellNode(entry.getValue().round(), entry.getValue().table(), entry.getKey().board(), entry.getKey().first(), entry.getKey().second()));
        }
        return result;
    }
}
