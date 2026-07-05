package thor.bridge_tournament.core.port.dto.board;

public record BoardDto(
        Integer id,
        int number,
        String dealer,
        String vulnerable
) {
}
