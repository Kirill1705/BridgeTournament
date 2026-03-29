package thor.bridge_tournament.core.calculation.movement;

import thor.bridge_tournament.core.exception.*;
import thor.bridge_tournament.core.type.board.BoardEntry;
import thor.bridge_tournament.core.type.tournament.TournamentNode;

import java.io.Serializable;
import java.util.*;

public class MovementValidator {
    public void validate(Collection<TournamentNode> nodes) {
        Set<NumberPair<UUID, UUID>> usedOpps = new HashSet<>();
        Set<NumberPair<UUID, UUID>> usedDeals = new HashSet<>();
        Set<NumberPair<UUID, Integer>> usedBoards = new HashSet<>();
        Set<NumberPair<Integer, Integer>> usedTables = new HashSet<>();
        Set<NumberPair<Integer, UUID>> pairUsed = new HashSet<>();
        for (TournamentNode node: nodes) {
            for (BoardEntry entry: node.getBoards()) {
                if (usedOpps.contains(new NumberPair<>(entry.getNs().uuid(), entry.getEw().uuid())) || usedOpps.contains(new NumberPair<>(entry.getEw().uuid(), entry.getNs().uuid()))) {
                    throw new PairUniqueMovementException(entry.getNs(), entry.getEw());
                }
                usedOpps.add(new NumberPair<>(entry.getNs().uuid(), entry.getEw().uuid()));
                usedOpps.add(new NumberPair<>(entry.getEw().uuid(), entry.getNs().uuid()));
                if (usedDeals.contains(new NumberPair<>(entry.getBoard().uuid(), entry.getNs().uuid()))) {
                    throw new DealsUniqueMovementException(entry.getBoard(), entry.getNs());
                }
                if (usedDeals.contains(new NumberPair<>(entry.getBoard().uuid(), entry.getEw().uuid()))) {
                    throw new DealsUniqueMovementException(entry.getBoard(), entry.getEw());
                }
                usedDeals.add(new NumberPair<>(entry.getBoard().uuid(), entry.getNs().uuid()));
                usedDeals.add(new NumberPair<>(entry.getBoard().uuid(), entry.getEw().uuid()));
                if (usedBoards.contains(new NumberPair<>(entry.getBoard().uuid(), node.getRound()))) {
                    throw new TheSameBoardInRoundException(entry.getBoard());
                }
                usedBoards.add(new NumberPair<>(entry.getBoard().uuid(), node.getRound()));
                if (usedTables.contains(new NumberPair<>(node.getTable(), node.getRound()))) {
                    throw new TheSameTableInRoundException(node.getRound(), node.getTable());
                }
                usedTables.add(new NumberPair<>(node.getTable(), node.getRound()));
                if (pairUsed.contains(new NumberPair<>(node.getRound(), entry.getNs().uuid()))) {
                    throw new PairTableUniqueException(entry.getNs(), node.getRound());
                }
                if (pairUsed.contains(new NumberPair<>(node.getRound(), entry.getEw().uuid()))) {
                    throw new PairTableUniqueException(entry.getEw(), node.getRound());
                }
                pairUsed.add(new NumberPair<>(node.getRound(), entry.getNs().uuid()));
                pairUsed.add(new NumberPair<>(node.getRound(), entry.getEw().uuid()));
            }
        }
    }

    private record NumberPair<T extends Serializable, U extends Serializable>(T first, U second) {}
}
