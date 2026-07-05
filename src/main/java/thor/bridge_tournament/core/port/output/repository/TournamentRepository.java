package thor.bridge_tournament.core.port.output.repository;

import thor.bridge_tournament.core.port.dto.tournament.TournamentDto;

import java.util.List;
import java.util.UUID;

public interface TournamentRepository {
    UUID save(TournamentDto tournamentDto);

    TournamentDto getById(UUID uuid);

    void addPlayerWithoutPair(UUID playerId, UUID tournamentId);

    void addPair(UUID pair, UUID tournamentId);

    List<UUID> getPlayersWithoutPair(UUID tournamentId);

    List<UUID> getAllPairs(UUID tournamentId);

    void removePlayersWithOutPairs(UUID tournamentId);
}
