package thor.bridge_tournament.infrastructure.mapping;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import thor.bridge_tournament.core.port.dto.board.BoardDto;
import thor.bridge_tournament.infrastructure.jpa.BoardEntity;

@Mapper(componentModel = "spring")
public interface BoardMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    BoardDto toDto(BoardEntity board);

    BoardEntity toJpa(BoardDto dto);
}
