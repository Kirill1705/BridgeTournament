package thor.bridge_tournament.presentation.telegram.session.state.tournament;

import lombok.Data;

@Data
public class TournamentCreationData {
    private int boards;
    private String countType;
    private String name;
    private int rounds;
}
