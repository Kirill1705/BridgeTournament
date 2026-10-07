package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import thor.bridge_tournament.core.domain.board.BoardEntry;
import thor.bridge_tournament.core.domain.CurrentTournamentManager;
import thor.bridge_tournament.core.domain.tournament.PairWithResult;
import thor.bridge_tournament.core.domain.tournament.Tournament;
import thor.bridge_tournament.core.exception.TournamentNotFoundException;
import thor.bridge_tournament.core.mapping.BoardEntryMapper;
import thor.bridge_tournament.core.port.dto.tournament_result.PairTournamentResultDto;
import thor.bridge_tournament.core.port.input.TournamentResultService;
import thor.bridge_tournament.core.port.output.repository.BoardEntryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Comparator;

@AllArgsConstructor
public class TournamentResultServiceImpl implements TournamentResultService {
    private final CurrentTournamentManager currentTournamentManager;
    private final BoardEntryRepository boardEntryRepository;

    @Override
    public List<PairTournamentResultDto> getRanks(UUID userId) {
        Tournament tournament;
        try {
            tournament = currentTournamentManager.getByPlayerId(userId);
        } catch (TournamentNotFoundException e) {
            tournament = currentTournamentManager.getByTd(userId);
        }
        List<BoardEntry> entries = boardEntryRepository.findByTournamentId(tournament.getUuid()).stream()
                .map(BoardEntryMapper::fromDto)
                .toList();
        Map<BoardEntry, Double> results = tournament.getCountType().createCalculator().calculate(entries);
        List<PairWithResult> tournamentResults = tournament.getCountType().createTournamentCalculator().calculate(results).stream()
                .sorted(Comparator.comparingDouble(PairWithResult::result).reversed().thenComparing(pair -> pair.pair().id())).toList();
        List<PairTournamentResultDto> extendedResults = new ArrayList<>();
        int rank = 1;
        double previousPoints = Double.NaN;
        for (PairWithResult pair: tournamentResults) {
            if (Double.compare(pair.result(), previousPoints) != 0) {
                rank = extendedResults.size() + 1;
            }
            extendedResults.add(new PairTournamentResultDto(
                    rank,
                    pair.pair(),
                    pair.result(),
                    tournament.getCountType().getFormatStandardName()
            ));
            previousPoints = pair.result();
        }
        return extendedResults;
    }
}
