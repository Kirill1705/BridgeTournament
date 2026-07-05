package thor.bridge_tournament.infrastructure.mapping;

import org.mapstruct.Mapper;
import thor.bridge_tournament.core.domain.tournament.TournamentNode;
import thor.bridge_tournament.infrastructure.jpa.TournamentNodeEntity;

@Mapper(componentModel = "spring")
public interface TournamentNodeMapper {
    TournamentNode toDto(TournamentNodeEntity entity);

    TournamentNodeEntity toJpa(TournamentNode node);
}
