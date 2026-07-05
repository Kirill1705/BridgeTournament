package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import thor.bridge_tournament.core.domain.board.Board;
import thor.bridge_tournament.core.domain.board.BoardEntry;
import thor.bridge_tournament.core.domain.movement.Movement;
import thor.bridge_tournament.core.domain.movement.MovementValidator;
import thor.bridge_tournament.core.domain.tournament.*;
import thor.bridge_tournament.core.exception.BoardNotFoundException;
import thor.bridge_tournament.core.mapping.BoardEntryMapper;
import thor.bridge_tournament.core.mapping.BoardMapper;
import thor.bridge_tournament.core.mapping.MovementMapper;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.board.BoardDto;
import thor.bridge_tournament.core.port.dto.board.BoardEntryDto;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.dto.movement.MovementDto;
import thor.bridge_tournament.core.port.dto.movement.PairMovementEntryDto;
import thor.bridge_tournament.core.port.dto.movement.PairMovementNextRoundInfo;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.input.MovementService;
import thor.bridge_tournament.core.port.output.TransactionalManager;
import thor.bridge_tournament.core.port.output.repository.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class MovementServiceImpl implements MovementService {
    private final CurrentTournamentManager currentTournamentManager;
    private final TournamentNodeRepository tournamentGamesRepository;
    private final UserRepository userRepository;
    private final BoardEntryRepository boardEntryRepository;
    private final TransactionalManager transactionalManager;
    private final BoardRepository boardRepository;
    private final MovementRepository movementRepository;

    @Override
    public PairBoardResult addTournamentBoardEntry(String userName, int boardNumber, RawBoardEntry entry) {
        UUID playerId = currentTournamentManager.getUserIdOrRegister(userName);
        Tournament tournament = currentTournamentManager.getByPlayerId(userName);
        Optional<BoardDto> boardDto = boardRepository.findByTournament(tournament.getUuid(), boardNumber);
        if (boardDto.isEmpty()) {
            throw new BoardNotFoundException(boardNumber);
        }
        Optional<TournamentNode> node = tournamentGamesRepository.findNode(playerId, tournament.getUuid(), boardNumber);
        return transactionalManager.executeTransactional(() -> {
            PairBoardResult result = addBoardEntry(userName, boardDto.get().id(), entry, tournament.getCountType().createCalculator(), node.get().ns(), node.get().ew(), tournament.getCountType().getFormatStandardName());
            Optional<UUID> entryId = boardEntryRepository.getEntryId(playerId, boardDto.get().id());
            tournamentGamesRepository.addEntry(entryId.get(), node.get().id());
            return result;
        });
    }

    @Override
    public PairBoardResult addBoardEntryImps(String username, int boardId, RawBoardEntry entry, Double throwAwayPercent) {
        if (throwAwayPercent == null) {
            throwAwayPercent = 0d;
        }
        return addBoardEntry(username, boardId, entry, new MedianImpsCalculator(throwAwayPercent, ImpTranslationScale.createDefault()), null, null, CountType.MEDIAN_IMPS.getFormatStandardName());
    }

    @Override
    public PairBoardResult addBoardEntryMp(String username, int boardId, RawBoardEntry entry) {
        throw new RuntimeException("Not implemented");
    }


    @Override
    public List<PairMovementEntryDto> getMovementCard(String userName) {
        Tournament tournament = currentTournamentManager.getByPlayerId(userName);
        UUID playerId = currentTournamentManager.getUserIdOrRegister(userName);
        List<TournamentNode> nodes = tournamentGamesRepository.getAllMovementsForPlayer(playerId, tournament.getUuid());
        return nodes.stream()
                .map(node -> new PairMovementEntryDto(
                        node.round(),
                        getOpponents(node, playerId),
                        node.table(),
                        node.boards()
                ))
                .toList();
    }

    @Override
    public PairMovementNextRoundInfo getMovementNextRound(String userName) {
        Tournament tournament = currentTournamentManager.getByPlayerId(userName);
        UUID playerId = currentTournamentManager.getUserIdOrRegister(userName);
        Optional<TournamentNode> node = tournamentGamesRepository.findNextNodeForPlayer(playerId, tournament.getUuid());
        PairDto opponents = getOpponents(node.get(), playerId);
        return new PairMovementNextRoundInfo(new PairMovementEntryDto(
                node.get().round(),
                opponents,
                node.get().table(),
                node.get().boards()
        ), tournamentGamesRepository.getDealsNotPlayed(node.get().id()));
    }

    @Override
    public void addMovement(MovementDto movementDto) {
        Movement movement = MovementMapper.fromDto(movementDto);
        MovementValidator validator = new MovementValidator();
        validator.validate(movement.getBody());
        Optional<UUID> movementOpt = movementRepository.find(movement.getPairsCount(), movementDto.roundsCount(), movementDto.type());
        transactionalManager.executeTransactional(() -> {
            if (movementOpt.isPresent()) {
                log.warn("Replacing movement");
                movementRepository.delete(movementOpt.get());
            }
            movementRepository.save(MovementMapper.toDto(movement));
        });
    }

    private PairBoardResult addBoardEntry(String username, int boardId, RawBoardEntry entry, BoardCalculator calculator, PairDto ns, PairDto ew, String countType) {
        UUID playerId = currentTournamentManager.getUserIdOrRegister(username);
        UserDto player = userRepository.getById(playerId);
        UUID boardEntryId = boardEntryRepository.getEntryId(playerId, boardId).orElse(null);
        Optional<BoardDto> boardDtoOpt = boardRepository.getById(boardId);
        if (boardDtoOpt.isEmpty()) {
            throw new BoardNotFoundException(boardId);
        }
        Board board = BoardMapper.fromDto(boardDtoOpt.get());
        BoardEntry boardEntry = BoardEntryMapper.fromDto(new BoardEntryDto(boardEntryId, BoardMapper.toDto(board), ns, ew, player, entry.contract(), entry.declarer(), entry.lead(), entry.result(), 0));
        UUID newBoardEntryId = boardEntryRepository.save(BoardEntryMapper.toDto(boardEntry));
        List<BoardEntry> entries = boardEntryRepository.findByBoardId(boardId).stream()
                .map(BoardEntryMapper::fromDto).toList();
        Map<BoardEntryDto, Double> results = calculator.calculate(entries).entrySet()
                .stream()
                .collect(Collectors.toMap(mapEntry -> BoardEntryMapper.toDto(mapEntry.getKey()), Map.Entry::getValue));
        return new PairBoardResult(countType, boardEntry.getPoints(), results.entrySet().stream()
                .filter(mapEntry -> mapEntry.getKey().id().equals(newBoardEntryId))
                .findAny().get().getValue()
                , results);
    }

    private PairDto getOpponents(TournamentNode node, UUID playerId) {
        if (node.ns().firstPlayer().id().equals(playerId) || node.ns().secondPlayer().id().equals(playerId)) {
            return node.ew();
        }
        return node.ns();
    }

}
