package thor.bridge_tournament.infrastructure.mapping;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import thor.bridge_tournament.core.port.dto.tournament.TournamentDto;
import thor.bridge_tournament.infrastructure.jpa.TournamentEntity;

@Mapper(componentModel = "spring")
public interface TournamentMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    TournamentDto toDto(TournamentEntity entity);

    TournamentEntity toJpa(TournamentDto dto);
}
