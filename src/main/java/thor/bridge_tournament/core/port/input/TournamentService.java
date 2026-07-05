package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.tournament.TournamentPlayers;

import java.util.List;

public interface TournamentService {
    void createTournament(String ownerUserName, int boards, String countType, String name, Integer rounds);

    void addTournamentDirector(String ownerUserName, String tdUserName);

    void addPlayer(String ownerUserName, String playerUserName);

    void addPair(String ownerUserName, String initiatorUserName, String partnerUserName);

    List<String> getTds(String tdUserName);

    TournamentPlayers getAllPlayers(String tdUserName);

    void startTournament(String ownerUserName);

    void addBoardToTournament(int boardId, String tdUserName);
}
