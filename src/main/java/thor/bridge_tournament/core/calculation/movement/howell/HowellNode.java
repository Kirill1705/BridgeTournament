package thor.bridge_tournament.core.calculation.movement.howell;

import java.util.Comparator;

public record HowellNode(int round, int table, int board, int first, int second) implements Comparable<HowellNode> {
    @Override
    public int compareTo(HowellNode o) {
        return Comparator.comparing(HowellNode::round)
                .thenComparing(HowellNode::table)
                .thenComparing(HowellNode::board)
                .thenComparing(HowellNode::first)
                .thenComparing(HowellNode::second)
                .compare(this, o);
    }
}
