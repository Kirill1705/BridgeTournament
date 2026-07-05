package thor.bridge_tournament.core.mapping;

import thor.bridge_tournament.core.domain.Contract;
import thor.bridge_tournament.core.domain.board.BoardEntry;
import thor.bridge_tournament.core.domain.board.Direction;
import thor.bridge_tournament.core.domain.card.Card;
import thor.bridge_tournament.core.port.dto.board.BoardEntryDto;

public class BoardEntryMapper {
    public static BoardEntryDto toDto(BoardEntry entry) {
        return new BoardEntryDto(
                entry.getUuid(),
                BoardMapper.toDto(entry.getBoard()),
                entry.getNs(),
                entry.getEw(),
                UserMapper.toDto(entry.getWriter()),
                entry.getContract().toStandardName(),
                entry.getDeclarer() != null ? entry.getDeclarer().name() : null,
                entry.getLead() != null ? entry.getLead().toName() : null,
                entry.getResult(),
                entry.getPoints()
        );
    }

    public static BoardEntry fromDto(BoardEntryDto dto) {
        return new BoardEntry(
                dto.id(),
                BoardMapper.fromDto(dto.board()),
                dto.ns(),
                dto.ew(),
                new Contract(dto.contract()),
                dto.declarer() != null ? Direction.valueOf(dto.declarer()) : null,
                dto.lead() != null ? new Card(dto.lead()) : null,
                dto.result(),
                UserMapper.fromDto(dto.writer())
        );
    }
}
