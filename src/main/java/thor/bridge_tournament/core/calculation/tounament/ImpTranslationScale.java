package thor.bridge_tournament.core.calculation.tounament;

import java.util.Collection;
import java.util.Collections;
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
}
