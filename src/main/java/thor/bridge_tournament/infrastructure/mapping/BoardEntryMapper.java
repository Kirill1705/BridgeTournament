package thor.bridge_tournament.infrastructure.mapping;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import thor.bridge_tournament.core.port.dto.board.BoardEntryDto;
import thor.bridge_tournament.infrastructure.jpa.BoardEntryEntity;

@Mapper(componentModel = "spring")
public interface BoardEntryMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "points", ignore = true)
    BoardEntryDto toDto(BoardEntryEntity entity);

    BoardEntryEntity toJpa(BoardEntryDto dto);
}
