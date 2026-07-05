package thor.bridge_tournament.core.domain.tournament;

import thor.bridge_tournament.core.exception.DomainValidationException;

public record ImpTranslationNode(int min, int max, int imps) implements Comparable<ImpTranslationNode> {
    public ImpTranslationNode {
        if (min>=max)
            throw new DomainValidationException("max should be greater then min");
        if (min < 0 || imps < 0)
            throw new DomainValidationException("imps and points should be greater then zero");
    }
    @Override
    public int compareTo(ImpTranslationNode node) {
        return Integer.compare(min, node.min);
    }
}
