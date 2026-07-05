package thor.bridge_tournament.core.domain.movement;

import lombok.Getter;
import thor.bridge_tournament.core.domain.tournament.MovementType;
import thor.bridge_tournament.core.domain.tournament.TournamentNode;
import thor.bridge_tournament.core.exception.DomainValidationException;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Movement {
    private final MovementType type;
    private final int pairsCount;
    private final int roundsCount;

    private final List<MovementBodyNode> body;

    public Movement(MovementType type, int pairsCount, int roundsCount, List<MovementBodyNode> body) {
        this.type = type;
        this.pairsCount = pairsCount;
        this.roundsCount = roundsCount;
        this.body = body;
        if (body == null || body.isEmpty()) {
            throw new DomainValidationException("Movement body shouldn't be empty");
        }
        for (MovementBodyNode node: body) {
            if (node.ns() <= 0 || node.ew() <= 0) {
                throw new DomainValidationException("Pair number should be greater then zero");
            }
            if (node.boardSetNumber() < 0) {
                throw new DomainValidationException("Number of board set should be not negative");
            }
            if (node.roundNumber() < 0) {
                throw new DomainValidationException("Round number should be not negative");
            }
        }
        if (pairsCount < 2) {
            throw new DomainValidationException("Pairs quantity should be 2 or more");
        }
        if (roundsCount <= 0) {
            throw new DomainValidationException("Rounds quantity should be greater then zero");
        }
    }

    public List<TournamentNode> scheduleTournament(BoardSelector selector, List<PairDto> pairs, UUID tournamentId) {
        List<TournamentNode> nodes = new ArrayList<>();
        for (MovementBodyNode movementNode: body) {
            nodes.add(new TournamentNode(
                    null,
                    tournamentId,
                    movementNode.roundNumber(),
                    movementNode.tableNumber(),
                    selector.select(movementNode.roundNumber()),
                    pairs.get(movementNode.ns()), pairs.get(movementNode.ew())
            ));
        }
        return nodes;
    }
}
