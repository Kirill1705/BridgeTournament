package thor.bridge_tournament.infrastructure.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import thor.bridge_tournament.core.domain.movement.MovementBodyNode;
import thor.bridge_tournament.core.port.dto.movement.MovementDto;
import thor.bridge_tournament.infrastructure.jpa.MovementEntity;

import java.util.ArrayList;
import java.util.List;

public class MovementMapper {
    public static MovementDto toDto(MovementEntity entity) {
        return new MovementDto(
                entity.getType(),
                entity.getPairsCount(),
                entity.getRoundsCount(),
                mapFromJson(entity.getBody(), entity.getPairsCount() * 2)
        );
    }

    public static MovementEntity toEntity(MovementDto dto) {
        MovementEntity entity = new MovementEntity();
        entity.setType(dto.type());
        entity.setRoundsCount(dto.roundsCount());
        entity.setPairsCount(dto.pairsCount());
        entity.setBody(mapToJson(dto.body()));
        return entity;
    }

    private static String mapToJson(List<MovementBodyNode> nodes) {
        ObjectMapper mapper = new ObjectMapper();
        List<List<Integer>> body = new ArrayList<>();
        for (MovementBodyNode node: nodes) {
            body.add(List.of(node.ns(), node.ew(), node.boardSetNumber()));
        }
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<MovementBodyNode> mapFromJson(String body, int tablesCount) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<MovementBodyNode> nodes = new ArrayList<>();
            int[][] data = mapper.readValue(body, int[][].class);
            for (int i = 0; i < data.length; i++) {
                nodes.add(new MovementBodyNode(data[i][0], data[i][1], data[i][2], i / tablesCount + 1, i % tablesCount));
            }
            return nodes;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
