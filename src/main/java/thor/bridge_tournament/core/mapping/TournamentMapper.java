package thor.bridge_tournament.core.mapping;

import thor.bridge_tournament.core.domain.tournament.CountType;
import thor.bridge_tournament.core.domain.tournament.Tournament;
import thor.bridge_tournament.core.port.dto.tournament.TournamentDto;

import java.util.HashSet;

public class TournamentMapper {
    public static TournamentDto toDto(Tournament tournament) {
        return new TournamentDto(
                tournament.getUuid(),
                tournament.getOwnerId(),
                tournament.getTds().stream().toList(),
                tournament.getCountType().name(),
                tournament.getBoards(),
                tournament.getRounds(),
                tournament.getName(),
                tournament.isStarted()
        );
    }

    public static Tournament fromDto(TournamentDto dto) {
        return new Tournament(
                dto.ownerId(),
                dto.boards(),
                dto.rounds(),
                dto.name(),
                dto.id(),
                new HashSet<>(dto.tds()),
                CountType.valueOf(dto.countType().toUpperCase()),
                dto.started()
        );
    }
}
