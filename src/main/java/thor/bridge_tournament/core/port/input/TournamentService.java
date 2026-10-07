package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.tournament.TournamentPlayers;
import thor.bridge_tournament.core.port.dto.UserDto;

import java.util.List;
import java.util.UUID;

public interface TournamentService {
    void createTournament(UUID ownerId, int boards, String countType, String name, Integer rounds);

    void addTournamentDirector(UUID ownerId, UUID tdId);

    void addPlayer(UUID ownerId, UUID playerId);

    void addPair(UUID ownerId, UUID initiatorId, UUID partnerId);

    List<UserDto> removePlayer(UUID tdId, UUID playerId);

    List<UserDto> getTds(UUID tdId);

    TournamentPlayers getAllPlayers(UUID tdId);

    void startTournament(UUID ownerId);

    void addBoardToTournament(int boardId, UUID tdId);
}
