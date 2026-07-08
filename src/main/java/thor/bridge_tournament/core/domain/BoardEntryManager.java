package thor.bridge_tournament.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import thor.bridge_tournament.core.domain.board.Board;
import thor.bridge_tournament.core.domain.board.BoardEntry;
import thor.bridge_tournament.core.domain.tournament.BoardCalculator;
import thor.bridge_tournament.core.exception.BoardNotFoundException;
import thor.bridge_tournament.core.mapping.BoardEntryMapper;
import thor.bridge_tournament.core.mapping.BoardMapper;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.board.BoardDto;
import thor.bridge_tournament.core.port.dto.board.BoardEntryDto;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.output.repository.BoardEntryRepository;
import thor.bridge_tournament.core.port.output.repository.BoardRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class BoardEntryManager {
    private final CurrentTournamentManager currentTournamentManager;
    private final BoardEntryRepository boardEntryRepository;
    private final BoardRepository boardRepository;

    public PairBoardResult addBoardEntry(String username, int boardId, RawBoardEntry entry, BoardCalculator calculator, PairDto ns, PairDto ew, String countType) {
        UUID playerId = currentTournamentManager.getUserIdOrRegister(username);
        UserDto player = currentTournamentManager.getUserRepository().getById(playerId);
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
        return new PairBoardResult(newBoardEntryId, countType, boardEntry.getPoints(), results.entrySet().stream()
                .filter(mapEntry -> mapEntry.getKey().id().equals(newBoardEntryId))
                .findAny().get().getValue()
                , results);
    }
}
