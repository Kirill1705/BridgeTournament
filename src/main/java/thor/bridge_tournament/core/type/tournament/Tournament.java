package thor.bridge_tournament.core.type.tournament;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import thor.bridge_tournament.core.exception.DomainValidationException;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Tournament {
    private final String ownerUserName;

    private UUID uuid;
    @Setter
    private String name;
    @Setter
    private Instant date;
    @Setter
    private MovementType movementType;
    private Set<String> tds = new HashSet<>();
    @Setter
    private CountType countType;
    private int boards;
    private boolean finished = false;

    public Tournament(String ownerUserName) {
        this.ownerUserName = ownerUserName;
    }

    public void finish() {
        checkFinished();
        finished = true;
    }

    public void setBoards(int boards) {
        checkFinished();
        if (boards <= 0)
            throw new DomainValidationException("boards quantity must be greater then zero");
        this.boards = boards;
    }

    public void addTd(String userName) {
        checkFinished();
        tds.add(userName);
    }

    private void checkFinished() {
        if (finished) {
            throw new RuntimeException("This tournament was finished and you cant edit it");
        }
    }
}
