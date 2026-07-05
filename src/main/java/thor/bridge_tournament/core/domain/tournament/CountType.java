package thor.bridge_tournament.core.domain.tournament;

import org.apache.commons.lang3.NotImplementedException;

public enum CountType {
    MEDIAN_IMPS,
    MP;
    public BoardCalculator createCalculator() {
        return switch (this) {
            case MEDIAN_IMPS -> new MedianImpsCalculator(0.1, ImpTranslationScale.createDefault());
            default -> throw new NotImplementedException();
        };
    }

    public TournamentCalculator createTournamentCalculator() {
        return switch (this) {
            case MEDIAN_IMPS -> new ImpTournamentCalculator();
            case MP -> new MpTournamentCalculator();
        };
    }

    public String getFormatStandardName() {
        return switch (this) {
            case MEDIAN_IMPS -> "IMP";
            case MP -> "MP";
        };
    }

    public static CountType fromStandardName(String name) {
        return switch (name) {
            case "IMP" -> CountType.MEDIAN_IMPS;
            case "MP" -> CountType.MP;
            default -> throw new IllegalStateException(name);
        };
    }
}
