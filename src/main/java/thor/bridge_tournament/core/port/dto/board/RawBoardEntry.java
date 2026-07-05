package thor.bridge_tournament.core.port.dto.board;

public record RawBoardEntry(String contract, String declarer, String lead, int result) {
}
