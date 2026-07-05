package thor.bridge_tournament.core.mapping;

import thor.bridge_tournament.core.domain.board.Board;
import thor.bridge_tournament.core.domain.board.Direction;
import thor.bridge_tournament.core.domain.board.Vulnerable;
import thor.bridge_tournament.core.port.dto.board.BoardDto;

public class BoardMapper {
    public static BoardDto toDto(Board board) {
        return new BoardDto(
                board.getId(),
                board.getNumber(),
                board.getDealer().name().toLowerCase(),
                board.getVulnerable().toString()
        );
    }

    public static Board fromDto(BoardDto boardDto) {
        return new Board(
                boardDto.id(),
                boardDto.number(),
                Direction.valueOf(boardDto.dealer().toUpperCase()),
                Vulnerable.fromName(boardDto.vulnerable())
        );
    }
}
