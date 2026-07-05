package thor.infrastructure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.input.BoardService;
import thor.bridge_tournament.core.port.input.MovementService;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = thor.bridge_tournament.BridgeTournamentApplication.class)
@Testcontainers
@ActiveProfiles("test")
@ComponentScan(basePackages = {
        "thor.bridge_tournament.core.service",
        "thor.bridge_tournament.infrastructure"
})
public class EntryTests {
    @Autowired
    private MovementService movementService;

    @Autowired
    private BoardService boardService;

    @Test
    public void addEntryTest_whenTwoEntriesAdd_ShouldCalculateResult() {
        int boardId = boardService.addBoard(1);
        RawBoardEntry firstEntry = new RawBoardEntry("4S", "N", "cK", 1);
        RawBoardEntry secondEntry = new RawBoardEntry("3S", "N", "cK", 1);
        movementService.addBoardEntryImps("user1", boardId, firstEntry, 0d);
        PairBoardResult result = movementService.addBoardEntryImps("user2", boardId, secondEntry, 0d);
        Assertions.assertEquals(170, result.points());
        Assertions.assertEquals(-4, result.duplicatePoints());
    }
}
