package thor.bridge_tournament.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import thor.bridge_tournament.core.domain.movement.*;
import thor.bridge_tournament.core.domain.tournament.*;
import thor.bridge_tournament.core.exception.DomainValidationException;
import thor.bridge_tournament.core.port.dto.movement.MovementDto;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.infrastructure.mapping.MovementMapper;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class MovementTests {
    @ParameterizedTest
    @ValueSource(ints = {6, 8, 10, 12, 14})
    void storedMovementsKeepRoundsPairsAndScheduleTheSpecifiedBoards(int pairCount) throws Exception {
        var original = readMovement(pairCount);
        var restored = MovementMapper.toDto(MovementMapper.toEntity(original));
        assertEquals(original, restored);
        var pairs = pairs(pairCount);
        var movement = thor.bridge_tournament.core.mapping.MovementMapper.fromDto(restored);
        var selector = new AverageBoardSelector(IntStream.rangeClosed(1, restored.roundsCount() * 2).boxed().toList(), restored.roundsCount());
        var nodes = movement.scheduleTournament(selector, pairs, UUID.randomUUID());
        assertEquals(restored.roundsCount() * pairCount / 2, nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            var expected = original.body().get(i);
            var actual = nodes.get(i);
            assertEquals(pairs.get(expected.ns() - 1), actual.ns());
            assertEquals(pairs.get(expected.ew() - 1), actual.ew());
            assertEquals(List.of(expected.boardSetNumber() * 2 - 1, expected.boardSetNumber() * 2), actual.boards());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 8, 10})
    void validMovementsAssignEachBoardSetOncePerPair(int pairCount) throws Exception {
        var dto = readMovement(pairCount);
        new MovementValidator().validate(dto.body());
        var pairs = pairs(pairCount);
        var movement = thor.bridge_tournament.core.mapping.MovementMapper.fromDto(dto);
        var selector = new AverageBoardSelector(IntStream.rangeClosed(1, dto.roundsCount() * 2).boxed().toList(), dto.roundsCount());
        var nodes = movement.scheduleTournament(selector, pairs, UUID.randomUUID());
        for (var pair : pairs) {
            var boards = nodes.stream().filter(node -> node.ns().equals(pair) || node.ew().equals(pair))
                    .flatMap(node -> node.boards().stream()).toList();
            assertEquals(dto.roundsCount() * 2, boards.size());
            assertEquals(boards.size(), new HashSet<>(boards).size());
        }
    }

    @Test
    void oddPairCountSkipsOnlyMeetingsWithTheAbsentPair() throws Exception {
        var dto = readMovement(6);
        var movement = thor.bridge_tournament.core.mapping.MovementMapper.fromDto(dto);
        var selector = new AverageBoardSelector(IntStream.rangeClosed(1, 8).boxed().toList(), 4);
        var pairs = pairs(5);
        var nodes = movement.scheduleTournament(selector, pairs, UUID.randomUUID());
        assertEquals(8, nodes.size());
        assertTrue(nodes.stream().allMatch(node -> pairs.contains(node.ns()) && pairs.contains(node.ew())));
    }

    @Test
    void rejectsZeroBasedBoardSetNumbers() {
        assertThrows(DomainValidationException.class, () -> new Movement(MovementType.HOWELL, 2, 1,
                List.of(new MovementBodyNode(1, 2, 0, 1, 0))));
    }

    private List<PairDto> pairs(int count) {
        return IntStream.range(0, count).mapToObj(i -> new PairDto(UUID.randomUUID(), null, null)).toList();
    }

    private MovementDto readMovement(int pairs) throws Exception {
        var json = new ObjectMapper().readTree(Path.of("movements", "howell" + pairs + ".json").toFile());
        List<MovementBodyNode> body = new ArrayList<>();
        for (int i = 0; i < json.get("body").size(); i++) {
            var node = json.get("body").get(i);
            body.add(new MovementBodyNode(node.get(0).asInt(), node.get(1).asInt(), node.get(2).asInt(), i / (pairs / 2) + 1, i % (pairs / 2)));
        }
        return new MovementDto(json.get("type").asText(), json.get("rounds").asInt(), pairs, body);
    }
}
