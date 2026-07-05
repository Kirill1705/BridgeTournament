package thor.bridge_tournament.infrastructure.mapping;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.infrastructure.jpa.PairEntity;

@Mapper(componentModel = "spring")
public interface PairMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    PairDto toDto(PairEntity entity);
    PairEntity toJpa(PairDto shortPairDto);
}