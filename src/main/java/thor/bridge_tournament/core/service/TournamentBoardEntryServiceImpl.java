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
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.input.TournamentBoardEntryService;
import thor.bridge_tournament.core.port.output.TransactionalManager;
import thor.bridge_tournament.core.port.output.repository.TournamentNodeRepository;

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
    public PairBoardResult addTournamentBoardEntryForPlayer(UUID userId, int boardNumber, RawBoardEntry entry) {
        return transactionalManager.executeTransactional(() -> {
            Tournament tournament = currentTournamentManager.getByPlayerId(userId);
            if (!tournament.isStarted()) {
                throw new DomainValidationException("Турнир ещё не начался");
            }
            BoardDto board = boardEntryManager.getBoardRepository().findByTournament(tournament.getUuid(), boardNumber)
                    .orElseThrow(() -> new BoardNotFoundException(boardNumber));
            TournamentNode node = tournamentNodeRepository.findNode(userId, tournament.getUuid(), boardNumber)
                    .orElseThrow(() -> new DomainValidationException("Этой сдачи нет в расписании вашей пары"));
            tournamentNodeRepository.lock(node.id());
            var written = boardEntryManager.getBoardEntryRepository().findByMeeting(node.id(), board.id());
            if (written.isPresent() && !written.get().writer().id().equals(userId)) {
                throw new DomainValidationException("Результат этой сдачи уже записан другим игроком или судьёй");
            }
            return saveTournamentEntry(tournament, node, board, userId, written.map(record -> record.id()).orElse(null), entry);
        });
    }

    @Override
    public PairBoardResult addTournamentBoardEntryByTd(UUID tdId, int boardNumber, RawBoardEntry entry,
                                                      UUID firstPairPlayerId, UUID secondPairPlayerId) {
        return transactionalManager.executeTransactional(() -> {
            Tournament tournament = currentTournamentManager.getByTd(tdId);
            if (!tournament.getTds().contains(tdId)) {
                throw new DomainValidationException("Вводить результаты за другие пары может только судья турнира");
            }
            if (!tournament.isStarted()) {
                throw new DomainValidationException("Турнир ещё не начался");
            }
            BoardDto board = boardEntryManager.getBoardRepository().findByTournament(tournament.getUuid(), boardNumber)
                    .orElseThrow(() -> new BoardNotFoundException(boardNumber));
            TournamentNode node = tournamentNodeRepository.findNode(firstPairPlayerId, tournament.getUuid(), boardNumber)
                    .orElseThrow(() -> new DomainValidationException("Указанной встречи нет в расписании турнира"));
            if (!(contains(node.ns(), firstPairPlayerId) && contains(node.ew(), secondPairPlayerId)
                    || contains(node.ew(), firstPairPlayerId) && contains(node.ns(), secondPairPlayerId))) {
                throw new DomainValidationException("Указанные игроки должны представлять две пары одной встречи на этой сдаче");
            }
            tournamentNodeRepository.lock(node.id());
            var written = boardEntryManager.getBoardEntryRepository().findByMeeting(node.id(), board.id());
            return saveTournamentEntry(tournament, node, board, tdId, written.map(record -> record.id()).orElse(null), entry);
        });
    }

    private boolean contains(PairDto pair, UUID playerId) {
        return pair != null && (pair.firstPlayer().id().equals(playerId) || pair.secondPlayer().id().equals(playerId));
    }

    private PairBoardResult saveTournamentEntry(Tournament tournament, TournamentNode node, BoardDto board,
                                                UUID writerId, UUID entryId, RawBoardEntry entry) {
        var calculator = tournament.getCountType().createCalculator();
        PairBoardResult result = boardEntryManager.saveBoardEntry(writerId, board.id(), entryId, entry, calculator,
                node.ns(), node.ew(), tournament.getCountType().getFormatStandardName());
        tournamentNodeRepository.addEntry(node.id(), result.entryId());
        var entries = boardEntryManager.getBoardEntryRepository().findByTournamentId(tournament.getUuid()).stream()
                .filter(record -> record.board().id().equals(board.id())).map(BoardEntryMapper::fromDto).toList();
        var protocol = calculator.calculate(entries).entrySet().stream()
                .collect(Collectors.toMap(record -> BoardEntryMapper.toDto(record.getKey()), Map.Entry::getValue));
        double points = protocol.entrySet().stream().filter(record -> record.getKey().id().equals(result.entryId()))
                .findFirst().orElseThrow().getValue();
        return new PairBoardResult(result.entryId(), result.countType(), result.points(), points, protocol);
    }
}
