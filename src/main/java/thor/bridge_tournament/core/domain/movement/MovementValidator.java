package thor.bridge_tournament.core.domain.movement;

import thor.bridge_tournament.core.exception.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MovementValidator {
    public void validate(Collection<MovementBodyNode> nodes) {
        Set<NumberPair> usedOpps = new HashSet<>();
        Set<NumberPair> usedDeals = new HashSet<>();
        Set<NumberPair> usedBoards = new HashSet<>();
        Set<NumberPair> usedTables = new HashSet<>();
        Set<NumberPair> pairUsed = new HashSet<>();
        for (MovementBodyNode node: nodes) {
            if (usedOpps.contains(new NumberPair(node.ns(), node.ew())) || usedOpps.contains(new NumberPair(node.ew(), node.ns()))) {
                throw new PairUniqueMovementException(node.ns(), node.ew());
            }
            usedOpps.add(new NumberPair(node.ns(), node.ew()));
            usedOpps.add(new NumberPair(node.ew(), node.ns()));
            if (usedDeals.contains(new NumberPair(node.boardSetNumber(), node.ns()))) {
                throw new DealsUniqueMovementException(node.boardSetNumber(), node.ns());
            }
            if (usedDeals.contains(new NumberPair(node.boardSetNumber(), node.ew()))) {
                throw new DealsUniqueMovementException(node.boardSetNumber(), node.ew());
            }
            usedDeals.add(new NumberPair(node.boardSetNumber(), node.ns()));
            usedDeals.add(new NumberPair(node.boardSetNumber(), node.ew()));
            if (usedBoards.contains(new NumberPair(node.boardSetNumber(), node.roundNumber()))) {
                throw new TheSameBoardInRoundException(node.boardSetNumber());
            }
            usedBoards.add(new NumberPair(node.boardSetNumber(), node.roundNumber()));
            if (usedTables.contains(new NumberPair(node.tableNumber(), node.roundNumber()))) {
                throw new TheSameTableInRoundException(node.roundNumber(), node.tableNumber());
            }
            usedTables.add(new NumberPair(node.tableNumber(), node.roundNumber()));
            if (pairUsed.contains(new NumberPair(node.roundNumber(), node.ns()))) {
                throw new PairTableUniqueException(node.ns(), node.roundNumber());
            }
            if (pairUsed.contains(new NumberPair(node.roundNumber(), node.ew()))) {
                throw new PairTableUniqueException(node.ew(), node.roundNumber());
            }
            pairUsed.add(new NumberPair(node.roundNumber(), node.ns()));
            pairUsed.add(new NumberPair(node.roundNumber(), node.ew()));
        }
    }

    private record NumberPair(int first, int second) {}
}
