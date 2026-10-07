package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import thor.bridge_tournament.core.domain.BoardEntryManager;
import thor.bridge_tournament.core.domain.CurrentTournamentManager;
import thor.bridge_tournament.core.domain.tournament.Tournament;
import thor.bridge_tournament.core.domain.tournament.TournamentNode;
import thor.bridge_tournament.core.exception.BoardNotFoundException;
import thor.bridge_tournament.core.exception.DomainValidationException;
import thor.bridge_tournament.core.mapping.BoardEntryMapper;
import thor.bridge_tournament.core.port.dto.board.BoardDto;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.input.TournamentBoardEntryService;
import thor.bridge_tournament.core.port.output.TransactionalManager;
import thor.bridge_tournament.core.port.output.repository.BoardRepository;
import thor.bridge_tournament.core.port.output.repository.TournamentNodeRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TournamentBoardEntryServiceImpl implements TournamentBoardEntryService {
    private final CurrentTournamentManager currentTournamentManager;
    private final TournamentNodeRepository tournamentNodeRepository;
    private final TransactionalManager transactionalManager;
    private final BoardEntryManager boardEntryManager;

    @Override
    public PairBoardResult addTournamentBoardEntry(UUID userId, int boardNumber, RawBoardEntry entry) {
        return transactionalManager.executeTransactional(() -> {
            Tournament tournament = currentTournamentManager.getByPlayerId(userId);
            if (!tournament.isStarted()) {
                throw new DomainValidationException("Турнир ещё не начался");
            }
            BoardDto board = boardEntryManager.getBoardRepository().findByTournament(tournament.getUuid(), boardNumber)
                    .orElseThrow(() -> new BoardNotFoundException(boardNumber));
            TournamentNode node = tournamentNodeRepository.findNode(userId, tournament.getUuid(), boardNumber)
                    .orElseThrow(() -> new DomainValidationException("Этой сдачи нет в расписании вашей пары"));
            if (!tournamentNodeRepository.getDealsNotPlayed(node.id()).contains(boardNumber)) {
                var written = boardEntryManager.getBoardEntryRepository().getEntryId(userId, board.id());
                if (written.isEmpty() || !tournamentNodeRepository.hasEntry(node.id(), written.get())) {
                    throw new DomainValidationException("Результат этой сдачи уже записан другим игроком");
                }
            }
            var calculator = tournament.getCountType().createCalculator();
            PairBoardResult result = boardEntryManager.addBoardEntry(userId, board.id(), entry, calculator,
                    node.ns(), node.ew(), tournament.getCountType().getFormatStandardName());
            tournamentNodeRepository.addEntry(node.id(), result.entryId());
            var entries = boardEntryManager.getBoardEntryRepository().findByTournamentId(tournament.getUuid()).stream()
                    .filter(record -> record.board().id().equals(board.id())).map(BoardEntryMapper::fromDto).toList();
            var protocol = calculator.calculate(entries).entrySet().stream()
                    .collect(Collectors.toMap(record -> BoardEntryMapper.toDto(record.getKey()), Map.Entry::getValue));
            double points = protocol.entrySet().stream().filter(record -> record.getKey().id().equals(result.entryId()))
                    .findFirst().orElseThrow().getValue();
            return new PairBoardResult(result.entryId(), result.countType(), result.points(), points, protocol);
        });
    }
}
