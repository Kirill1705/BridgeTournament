package thor.bridge_tournament.core.domain.tournament;

import lombok.AllArgsConstructor;
import lombok.Getter;
import thor.bridge_tournament.core.domain.board.Board;
import thor.bridge_tournament.core.exception.DomainValidationException;

import java.util.*;

@Getter
@AllArgsConstructor
public class Tournament {
    private final UUID ownerId;

    private final int boards;

    private final Integer rounds;

    private String name;

    private UUID uuid;

    private Set<UUID> tds = new HashSet<>();

    private CountType countType;

    private boolean started = false;

    public Tournament(UUID ownerId, int boards, Integer rounds, CountType countType, String name) {
        this.rounds = rounds;
        if (boards <= 0)
            throw new DomainValidationException("boards quantity must be greater then zero");
        this.ownerId = ownerId;
        this.boards = boards;
        this.countType = countType;
        this.name = name;
        tds.add(ownerId);
    }

    public void start() {
        checkStarted();
        started = true;
    }

    public void addTd(UUID tdId) {
        checkStarted();
        tds.add(tdId);
    }

    private void checkStarted() {
        if (started) {
            throw new RuntimeException("This tournament was started and you cant edit it");
        }
    }

    public List<Integer> getPossibleRoundsQuantity() {
        List<Integer> result = new ArrayList<>();
        if (rounds != null) {
            result.add(rounds);
        }
        for (int boardSetSize = 2; boardSetSize <= boards/2; boardSetSize++) {
            if (boards % boardSetSize == 0) {
                result.add(boards / boardSetSize);
            }
        }
        result.add(boards);
        Set<Integer> used = new HashSet<>(result);
        for (int rounds = boards; rounds > 0; rounds --) {
            if (!used.contains(rounds)) {
                result.add(rounds);
            }
        }
        return result;
    }

    public List<Board> generateBoards(Set<Integer> usedBoardNumbers) {
        List<Board> boardList = new ArrayList<>();
        for (int i = 0; i < boards; i++) {
            if (!usedBoardNumbers.contains(i)) {
                boardList.add(new Board(i));
            }
        }
        return boardList;
    }
}
