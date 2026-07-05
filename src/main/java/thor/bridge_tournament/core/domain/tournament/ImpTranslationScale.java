package thor.bridge_tournament.core.domain.tournament;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class ImpTranslationScale {
    private final TreeSet<ImpTranslationNode> nodes = new TreeSet<>();

    public Collection<ImpTranslationNode> getNodes() {
        return Collections.unmodifiableCollection(nodes);
    }

    public ImpTranslationScale(Collection<ImpTranslationNode> nodes) {
        this.nodes.addAll(nodes);
        ImpTranslationNode prev = null;
        for (ImpTranslationNode node: this.nodes) {
            if (prev == null) {
                prev = node;
                continue;
            }
            if (node.min() <= prev.max() || node.imps() != prev.imps() + 1)
                throw new RuntimeException("Invalid imp translation scale");
            prev = node;
        }
    }

    public int translate(int points) {
        if (points < 0)
            throw new RuntimeException("points should be greater then zero");
        ImpTranslationNode node = nodes.floor(new ImpTranslationNode(points, points + 10, 1));
        if (node == null)
            throw new RuntimeException("very big value of points");
        return node.imps();
    }

    public static ImpTranslationScale createDefault() {
        return new ImpTranslationScale(List.of(
                new ImpTranslationNode(0, 10, 0),
                new ImpTranslationNode(20, 40, 1),
                new ImpTranslationNode(50, 80, 2),
                new ImpTranslationNode(90, 120, 3),
                new ImpTranslationNode(130, 160, 4),
                new ImpTranslationNode(170, 210, 5),
                new ImpTranslationNode(220, 260, 6),
                new ImpTranslationNode(270, 310, 7),
                new ImpTranslationNode(320, 360, 8),
                new ImpTranslationNode(370, 420, 9),
                new ImpTranslationNode(430, 490, 10),
                new ImpTranslationNode(500, 590, 11),
                new ImpTranslationNode(600, 740, 12),
                new ImpTranslationNode(750, 890, 13),
                new ImpTranslationNode(900, 1090, 14),
                new ImpTranslationNode(1100, 1290, 15),
                new ImpTranslationNode(1300, 1490, 16)
        ));
    }
}
